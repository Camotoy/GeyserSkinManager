package com.github.camotoy.geyserskinmanager.spigot;

import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.spigot.listener.EventListener;
import com.github.camotoy.geyserskinmanager.spigot.listener.PaperEventListener;
import com.github.camotoy.geyserskinmanager.spigot.listener.SpigotEventListener;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class GeyserSkinManager extends JavaPlugin {
    private EventListener listener;

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
