package com.github.camotoy.geyserskinmanager.bungeecord;

import com.github.camotoy.geyserskinmanager.common.platform.BedrockSkinUtilityListener;
import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.SkinDatabase;
import com.github.camotoy.geyserskinmanager.common.skinretriever.BedrockSkinRetriever;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class BungeecordBedrockSkinUtilityListener extends BedrockSkinUtilityListener<ProxiedPlayer> implements Listener {

    public BungeecordBedrockSkinUtilityListener(SkinDatabase database, BedrockSkinRetriever skinRetriever) {
        super(database, skinRetriever);
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        event.getPlayer().sendData(Constants.INIT_PLUGIN_MESSAGE_NAME, new byte[0]);
    }

    @EventHandler
    public void onPluginMessageReceived(PluginMessageEvent event) {
        if (event.getTag().equals(Constants.BEDROCK_SKIN_UTILITY_INIT_NAME)) {
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()))) {
                int version = in.readInt();
                if (version != Constants.BEDROCK_SKIN_UTILITY_INIT_VERSION) {
                    // Ignore I guess; we wouldn't want to spam the server
                    return;
                }

                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(new UUID(in.readLong(), in.readLong()));
                if (player == null) {
                    return;
                }
                if (!moddedPlayers.containsKey(player.getUniqueId())) {
                    moddedPlayers.put(player.getUniqueId(), player);
                    sendAllCapes(player);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onBedrockPlayerJoin(ProxiedPlayer player, ServerInfo serverInfo) {
        byte[] payload = getCape(getUUID(player));
        if (payload != null) {
            for (ProxiedPlayer moddedPlayer : moddedPlayers.values()) {
                if (moddedPlayer.getServer().getInfo().getName().equals(serverInfo.getName())) {
                    sendCape(payload, moddedPlayer);
                }
            }
        }
    }

    @Override
    public void sendAllCapes(ProxiedPlayer player) {
        for (Map.Entry<UUID, byte[]> cape : database.getCapes()) {
            if (player.getServer().getInfo().getPlayers().contains(ProxyServer.getInstance().getPlayer(cape.getKey()))) {
                sendCape(cape.getValue(), player);
            }
        }
    }

    @Override
    public void sendCape(byte[] payload, ProxiedPlayer player) {
        player.sendData(Constants.CAPE_PLUGIN_MESSAGE_NAME, payload);
    }

    @Override
    public UUID getUUID(ProxiedPlayer player) {
        return player.getUniqueId();
    }
}
