package com.github.camotoy.geyserskinmanager.spigot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class GeyserSkinManager extends JavaPlugin {

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("Geyser-Spigot") == null) {
            getLogger().severe("This plugin requires Geyser-Spigot to be installed!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();
        }

        EventListener listener = new PaperEventListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
    }
}
