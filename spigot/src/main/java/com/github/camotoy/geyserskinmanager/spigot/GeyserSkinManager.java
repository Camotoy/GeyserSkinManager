package com.github.camotoy.geyserskinmanager.spigot;

import com.github.camotoy.geyserskinmanager.common.Configuration;
import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.FloodgateUtil;
import com.github.camotoy.geyserskinmanager.spigot.listener.BungeecordPluginMessageListener;
import com.github.camotoy.geyserskinmanager.spigot.listener.SpigotPlatformEventListener;
import com.github.camotoy.geyserskinmanager.spigot.listener.PaperEventListener;
import com.github.camotoy.geyserskinmanager.spigot.listener.SpigotEventListener;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class GeyserSkinManager extends JavaPlugin {
    private SpigotPlatformEventListener listener;

    @Override
    public void onEnable() {
        Configuration config = Configuration.create(this.getDataFolder().toPath());
        boolean floodgatePresent = FloodgateUtil.isFloodgatePresent(config, getLogger()::warning);
        boolean bungeeCordMode = Bukkit.getPluginManager().getPlugin("Geyser-Spigot") == null;

        if (!bungeeCordMode) {
            if (PaperLib.isPaper() && PaperLib.isVersion(12, 2)) {
                this.listener = new PaperEventListener(this, !floodgatePresent);
            } else {
                this.listener = new SpigotEventListener(this, !floodgatePresent);
            }

            Bukkit.getPluginManager().registerEvents(listener, this);
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, Constants.MOD_PLUGIN_MESSAGE_NAME);
        } else {
            getLogger().info("We are in BungeeCord mode as there is no Geyser-Spigot plugin installed.");
            Bukkit.getMessenger().registerIncomingPluginChannel(this, Constants.SKIN_PLUGIN_MESSAGE_NAME, new BungeecordPluginMessageListener(this));
        }
    }

    @Override
    public void onDisable() {
        if (this.listener != null) {
            this.listener.shutdown();
        }
    }
}
