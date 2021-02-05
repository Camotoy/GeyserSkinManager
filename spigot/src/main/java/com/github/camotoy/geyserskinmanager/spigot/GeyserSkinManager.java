package com.github.camotoy.geyserskinmanager.spigot;

import com.github.camotoy.geyserskinmanager.common.Constants;
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
        if (!getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();
        }

        boolean bungeeCordMode = Bukkit.getPluginManager().getPlugin("Geyser-Spigot") == null;

        if (PaperLib.isPaper() && PaperLib.isVersion(12, 2)) {
            this.listener = new PaperEventListener(this, bungeeCordMode);
        } else {
            this.listener = new SpigotEventListener(this, bungeeCordMode);
        }
        if (!bungeeCordMode) {
            Bukkit.getPluginManager().registerEvents(listener, this);
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, Constants.CAPE_PLUGIN_MESSAGE_NAME);
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, Constants.INIT_PLUGIN_MESSAGE_NAME);
        } else {
            getLogger().info("We are in BungeeCord mode as there is no Geyser-Spigot plugin installed.");
            Bukkit.getMessenger().registerIncomingPluginChannel(this, Constants.SKIN_PLUGIN_MESSAGE_NAME, listener);
        }
    }

    @Override
    public void onDisable() {
        if (this.listener != null) {
            this.listener.shutdown();
        }
    }
}
