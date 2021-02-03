package com.github.camotoy.geyserskinmanager.bungeecord;

import net.md_5.bungee.api.plugin.Plugin;

public final class GeyserSkinManager extends Plugin {

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();
        }

        getProxy().getPluginManager().registerListener(this, new BungeecordEventListener(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
