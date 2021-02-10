package com.github.camotoy.geyserskinmanager.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Paths;

@Plugin(
        id = "geyserskinmanager-velocity",
        name = "GeyserSkinManager-Velocity",
        version = "1.4-SNAPSHOT",
        authors = {"Camotoy"},
        dependencies = {@Dependency(id = "geyser")}
)
public class GeyserSkinManager {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        File dataFolder = Paths.get("plugins/GeyserSkinManager-Velocity").toFile();
        if (!dataFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dataFolder.mkdirs();
        }
        server.getEventManager().register(this, new VelocitySkinEventListener(server, this, dataFolder, logger));
    }

    public Logger getLogger() {
        return logger;
    }
}
