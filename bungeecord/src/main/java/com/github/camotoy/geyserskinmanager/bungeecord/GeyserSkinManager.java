package com.github.camotoy.geyserskinmanager.bungeecord;

import com.github.camotoy.geyserskinmanager.common.Configuration;
import com.github.camotoy.geyserskinmanager.common.FloodgateUtil;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;

public final class GeyserSkinManager extends Plugin {
    private BungeecordSkinEventListener listener;

    @Override
    public void onEnable() {
        try {
            new Configuration(this.getDataFolder().toPath());
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
}
