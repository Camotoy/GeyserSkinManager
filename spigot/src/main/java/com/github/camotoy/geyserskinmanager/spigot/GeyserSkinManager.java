package com.github.camotoy.geyserskinmanager.spigot;

import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.FloodgateUtil;
import com.github.camotoy.geyserskinmanager.spigot.listener.BungeecordPluginMessageListener;
import com.github.camotoy.geyserskinmanager.spigot.listener.SpigotPlatformEventListener;
import com.github.camotoy.geyserskinmanager.spigot.listener.PaperEventListener;
import com.github.camotoy.geyserskinmanager.spigot.listener.SpigotEventListener;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class GeyserSkinManager extends JavaPlugin {
    private SpigotPlatformEventListener listener;

    @Override
    public void onEnable() {
        createConfig();
        FloodgateUtil.setForceSkin(getConfig().getBoolean("ForceShowSkins"));
        boolean floodgatePresent = FloodgateUtil.isFloodgatePresent(getLogger()::warning);
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

    private void createConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (this.listener != null) {
            this.listener.shutdown();
        }
    }
}
