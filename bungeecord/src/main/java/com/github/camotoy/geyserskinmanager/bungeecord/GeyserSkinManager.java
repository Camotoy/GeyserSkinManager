package com.github.camotoy.geyserskinmanager.bungeecord;

import net.md_5.bungee.api.plugin.Plugin;

public final class GeyserSkinManager extends Plugin {
    private BungeecordEventListener listener;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();
        }

        this.listener = new BungeecordEventListener(this);
        getProxy().getPluginManager().registerListener(this, this.listener);
    }

    @Override
    public void onDisable() {
        if (this.listener != null) {
            this.listener.shutdown();
        }
    }
}
