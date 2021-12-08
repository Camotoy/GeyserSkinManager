package com.github.camotoy.geyserskinmanager.velocity;

import com.github.camotoy.geyserskinmanager.common.Configuration;
import com.github.camotoy.geyserskinmanager.common.FloodgateUtil;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "geyserskinmanager-velocity",
        name = "GeyserSkinManager-Velocity",
        version = "1.6",
        authors = {"Camotoy"},
        dependencies = {@Dependency(id = "geyser")}
)
public class GeyserSkinManager {
    private final ProxyServer server;
    private final Path dataDirectory;

    @Inject
    private Logger logger;

    @Inject
    public GeyserSkinManager(ProxyServer server, @DataDirectory final Path folder) {
        this.server = server;
        this.dataDirectory = folder;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Configuration config = Configuration.create(this.dataDirectory);
        boolean floodgatePresent = FloodgateUtil.isFloodgatePresent(config, getLogger()::warn);
        server.getEventManager().register(this, new VelocitySkinEventListener(server, this, dataDirectory, logger, !floodgatePresent));
    }

    public Logger getLogger() {
        return logger;
    }
}
