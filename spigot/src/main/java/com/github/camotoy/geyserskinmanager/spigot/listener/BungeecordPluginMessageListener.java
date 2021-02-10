package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.SkinEntry;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import com.github.camotoy.geyserskinmanager.spigot.SpigotSkinApplier;
import com.github.camotoy.geyserskinmanager.spigot.profile.GameProfileWrapper;
import com.github.camotoy.geyserskinmanager.spigot.profile.MinecraftProfileWrapper;
import com.github.camotoy.geyserskinmanager.spigot.profile.PaperProfileWrapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class BungeecordPluginMessageListener implements Listener, PluginMessageListener {
    private final GeyserSkinManager plugin;
    private final Function<Player, MinecraftProfileWrapper> getProfileFunction;
    private final SpigotSkinApplier skinApplier;

    /**
     * Information is stored here in the event that a plugin message is received before the player has joined.
     */
    private final Cache<UUID, SkinEntry> skinEntryCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    public BungeecordPluginMessageListener(GeyserSkinManager plugin) {
        this.plugin = plugin;
        this.skinApplier = new SpigotSkinApplier(plugin);
        this.getProfileFunction = (PaperLib.isPaper() && PaperLib.isVersion(12, 2)) ?
                 PaperProfileWrapper::from : GameProfileWrapper::from;

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        SkinEntry skinEntry = skinEntryCache.getIfPresent(event.getPlayer().getUniqueId());
        if (skinEntry != null) {
            skinApplier.setSkin(getProfileFunction.apply(event.getPlayer()), event.getPlayer(), skinEntry);
        }
    }

    @Override
    public void onPluginMessageReceived(@Nonnull String channel, @Nonnull Player player, @Nonnull byte[] message) {
        if (!channel.equals(Constants.SKIN_PLUGIN_MESSAGE_NAME)) {
            return;
        }

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            int version = in.readInt();
            if (version != Constants.SKIN_PLUGIN_MESSAGE_VERSION) {
                plugin.getLogger().warning("Received a plugin message with an invalid version! Make sure that GeyserSkinManager is updated on both BungeeCord and backend servers!");
                return;
            }
            UUID uuid = new UUID(in.readLong(), in.readLong());
            String value = in.readUTF();
            String signature = in.readUTF();
            SkinEntry skinEntry = new SkinEntry(value, signature);

            Player bedrockPlayer = Bukkit.getPlayer(uuid);
            if (bedrockPlayer == null) {
                // Wait until they have officially joined
                skinEntryCache.put(uuid, skinEntry);
                return;
            }
            skinApplier.setSkin(getProfileFunction.apply(bedrockPlayer), bedrockPlayer, skinEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
