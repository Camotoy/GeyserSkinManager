package com.github.camotoy.geyserskinmanager.velocity;

import com.github.camotoy.geyserskinmanager.common.FloodgateUtil;
import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
        id = "geyserskinmanager-velocity",
        name = "GeyserSkinManager-Velocity",
        version = "1.5-SNAPSHOT",
        authors = {"Camotoy"},
        dependencies = {@Dependency(id = "geyser")}
)
public class GeyserSkinManager {


    private final ProxyServer server;
    private final Path dataDirectory;
    private final Toml config;

    @Inject
    private Logger logger;

    @Inject
    public GeyserSkinManager(ProxyServer server,  @DataDirectory final Path folder) {
        this.server  = server;
        this.dataDirectory = folder;
        this.config = loadConfig(dataDirectory);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        FloodgateUtil.setForceSkin(config.getBoolean("ForceShowSkins"));
        boolean floodgatePresent = FloodgateUtil.isFloodgatePresent(getLogger()::warn);
        server.getEventManager().register(this, new VelocitySkinEventListener(server, this, dataDirectory, logger, !floodgatePresent));
    }
    /**
     * Load GeyserSkinManager config
     *
     * @param path The config's directory
     * @return The configuration
     */
    private Toml loadConfig(Path path) {
        File folder = path.toFile();
        File file = new File(folder, "config.toml");

        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (InputStream input = getClass().getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                return null;
            }
        }
        return new Toml().read(file);
    }
    public Logger getLogger() {
        return logger;
    }
}
