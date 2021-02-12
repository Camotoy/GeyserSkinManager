package com.github.camotoy.geyserskinmanager.velocity;

import com.github.camotoy.geyserskinmanager.common.BedrockPluginMessageData;
import com.github.camotoy.geyserskinmanager.common.RawSkin;
import com.github.camotoy.geyserskinmanager.common.SkinDatabase;
import com.github.camotoy.geyserskinmanager.common.platform.BedrockSkinUtilityListener;
import com.github.camotoy.geyserskinmanager.common.skinretriever.BedrockSkinRetriever;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChannelRegisterEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;

import java.util.Map;
import java.util.UUID;

public class VelocityBedrockSkinUtilityListener extends BedrockSkinUtilityListener<Player> {
    private final ProxyServer server;

    public VelocityBedrockSkinUtilityListener(ProxyServer server, SkinDatabase database, BedrockSkinRetriever skinRetriever) {
        super(database, skinRetriever);
        this.server = server;
    }

    @Subscribe
    public void onPluginMessageReceived(PlayerChannelRegisterEvent event) {
        if (event.getChannels().contains(VelocityConstants.MOD_PLUGIN_MESSAGE_NAME)) {
            onModdedPlayerConfirm(event.getPlayer());
        }
    }

    @Override
    public void onBedrockPlayerJoin(Player player, RawSkin skin) {
        BedrockPluginMessageData data = getSkinAndCape(player.getUniqueId(), skin);
        if (data != null) {
            onBedrockServerJoinOrSwitch(player, data);
        }
    }

    public void onBedrockServerJoinOrSwitch(Player bedrockPlayer, BedrockPluginMessageData data) {
        bedrockPlayer.getCurrentServer().ifPresent(connection -> {
            for (Player moddedPlayer : moddedPlayers.values()) {
                if (connection.getServer().getPlayersConnected().contains(moddedPlayer)) {
                    sendPluginMessageData(moddedPlayer, data);
                }
            }
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void onServerConnected(ServerPostConnectEvent event) {
        if (event.getPreviousServer() != null) {
            if (this.moddedPlayers.containsKey(event.getPlayer().getUniqueId())) {
                sendAllCapes(event.getPlayer());
            }

            BedrockPluginMessageData data = this.database.getPluginMessageData(event.getPlayer().getUniqueId());
            if (data != null) {
                onBedrockServerJoinOrSwitch(event.getPlayer(), data);
            }
        }
    }

    @Override
    public void sendAllCapes(Player player) {
        ServerConnection connection = player.getCurrentServer().orElse(null);
        if (connection == null) {
            // ???
            return;
        }

        for (Map.Entry<UUID, BedrockPluginMessageData> data : database.getPluginMessageData()) {
            server.getPlayer(data.getKey()).ifPresent(otherPlayer -> {
                if (connection.getServer().getPlayersConnected().contains(otherPlayer)) {
                    sendPluginMessageData(player, data.getValue());
                }
            });
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        onPlayerLeave(event.getPlayer());
    }

    @Override
    public void sendPluginMessage(byte[] payload, Player player) {
        player.sendPluginMessage(VelocityConstants.MOD_PLUGIN_MESSAGE_NAME, payload);
    }

    @Override
    public UUID getUUID(Player player) {
        return player.getUniqueId();
    }
}
