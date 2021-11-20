package com.github.camotoy.geyserskinmanager.bungeecord;

import com.github.camotoy.geyserskinmanager.common.FloodgateUtil;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class GeyserSkinManager extends Plugin {
    private BungeecordSkinEventListener listener;

    @Override
    public void onEnable() {
        createConfig();
        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            FloodgateUtil.setForceSkin(config.getBoolean("ForceShowSkins"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean floodgatePresent = FloodgateUtil.isFloodgatePresent(getLogger()::warning);
        this.listener = new BungeecordSkinEventListener(this, !floodgatePresent);
        getProxy().getPluginManager().registerListener(this, this.listener);
    }

    @Override
    public void onDisable() {
        if (this.listener != null) {
            this.listener.shutdown();
        }
    }
    public void createConfig() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
