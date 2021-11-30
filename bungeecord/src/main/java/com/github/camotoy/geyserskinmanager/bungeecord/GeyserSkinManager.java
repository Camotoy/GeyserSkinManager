package com.github.camotoy.geyserskinmanager.bungeecord;

import com.github.camotoy.geyserskinmanager.common.Configuration;
import com.github.camotoy.geyserskinmanager.common.FloodgateUtil;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;

public final class GeyserSkinManager extends Plugin {
    private BungeecordSkinEventListener listener;

    @Override
    public void onEnable() {
        Configuration.dataDirectory = this.getDataFolder().toPath();
        new Configuration();
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
