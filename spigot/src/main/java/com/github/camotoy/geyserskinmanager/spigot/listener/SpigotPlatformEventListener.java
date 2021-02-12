package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.github.camotoy.geyserskinmanager.common.SkinEntry;
import com.github.camotoy.geyserskinmanager.common.platform.SkinEventListener;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import com.github.camotoy.geyserskinmanager.spigot.SpigotSkinApplier;
import com.github.camotoy.geyserskinmanager.spigot.profile.MinecraftProfileWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public abstract class SpigotPlatformEventListener extends SkinEventListener<Player, MinecraftProfileWrapper> implements Listener {
    protected final SpigotBedrockSkinUtilityListener modListener;
    protected final GeyserSkinManager plugin;
    protected final SpigotSkinApplier skinApplier;

    public SpigotPlatformEventListener(GeyserSkinManager plugin, boolean bungeeCordMode) {
        super(plugin.getDataFolder(), plugin.getLogger()::warning);
        this.plugin = plugin;

        if (bungeeCordMode) {
            // BungeeCord takes care of the database, so we don't need to
            this.modListener = null;
        } else {
            this.modListener = new SpigotBedrockSkinUtilityListener(this.plugin, this.database, this.skinRetriever);
            Bukkit.getPluginManager().registerEvents(this.modListener, this.plugin);
        }

        this.skinApplier = new SpigotSkinApplier(plugin);
    }

    @EventHandler
    public abstract void onPlayerJoin(PlayerJoinEvent event);

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        this.modListener.onPlayerLeave(event.getPlayer());
    }

    public void shutdown() {
        if (database != null) {
            database.clear();
        }
    }

    @Override
    public void onSuccess(Player player, MinecraftProfileWrapper profile, SkinEntry skinEntry) {
        skinApplier.setSkin(profile, player, skinEntry);
    }

    @Override
    public UUID getUUID(Player player) {
        return player.getUniqueId();
    }
}
