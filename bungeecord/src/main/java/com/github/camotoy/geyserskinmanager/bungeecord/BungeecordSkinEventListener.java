package com.github.camotoy.geyserskinmanager.bungeecord;

import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.RawSkin;
import com.github.camotoy.geyserskinmanager.common.SkinEntry;
import com.github.camotoy.geyserskinmanager.common.platform.ProxyPluginMessageSend;
import com.github.camotoy.geyserskinmanager.common.platform.SkinEventListener;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class BungeecordSkinEventListener extends SkinEventListener<ProxiedPlayer, Server> implements Listener, ProxyPluginMessageSend<Server> {
    private final boolean showSkins;
    private final BungeecordBedrockSkinUtilityListener modListener;

    public BungeecordSkinEventListener(GeyserSkinManager plugin, boolean showSkins) {
        super(plugin.getDataFolder().toPath(), plugin.getLogger()::warning);
        this.modListener = new BungeecordBedrockSkinUtilityListener(database, skinRetriever);
        this.showSkins = showSkins;

        plugin.getProxy().registerChannel(Constants.MOD_PLUGIN_MESSAGE_NAME);
        plugin.getProxy().getPluginManager().registerListener(plugin, this.modListener);
    }

    public void shutdown() {
        this.database.clear();
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        if (event.getPlayer().getUniqueId().version() == 4) {
            // Linked player that probably already has a skin, or an online mode player
            return;
        }

        RawSkin skin = this.skinRetriever.getBedrockSkin(event.getPlayer().getUniqueId());
        if (skin != null && showSkins) {
            uploadOrRetrieveSkin(event.getPlayer(), event.getServer(), skin);
        }
        if (skin != null || this.skinRetriever.isBedrockPlayer(event.getPlayer().getUniqueId())) {
            this.modListener.onBedrockPlayerJoin(event.getPlayer(), skin, event.getServer().getInfo());
        }
    }

    @EventHandler
    public void onProxyLeave(PlayerDisconnectEvent event) {
        this.modListener.onPlayerLeave(event.getPlayer());
    }

    @Override
    public void onSuccess(ProxiedPlayer player, Server server, SkinEntry skinEntry) {
        UUID uuid = getUUID(player);
        sendSkinToBackendServer(uuid, server, skinEntry);
    }

    @Override
    public UUID getUUID(ProxiedPlayer player) {
        return player.getUniqueId();
    }

    @Override
    public void sendPluginMessage(Server server, byte[] payload) {
        server.sendData(Constants.SKIN_PLUGIN_MESSAGE_NAME, payload);
    }
}
