package com.github.camotoy.geyserskinmanager.spigot;

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

        if (PaperLib.isPaper() && PaperLib.isVersion(12, 2)) {
            this.listener = new PaperEventListener(this);
        } else {
            this.listener = new SpigotEventListener(this);
        }
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        if (this.listener != null) {
            this.listener.shutdown();
        }
    }
}
