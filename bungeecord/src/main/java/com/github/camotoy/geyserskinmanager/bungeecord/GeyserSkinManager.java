package com.github.camotoy.geyserskinmanager.bungeecord;

import com.github.camotoy.geyserskinmanager.common.Configuration;
import com.github.camotoy.geyserskinmanager.common.FloodgateUtil;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class GeyserSkinManager extends Plugin {
    private BungeecordSkinEventListener listener;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();
        }
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        new Configuration().loadConfigFile(getDataFolder());
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
}
