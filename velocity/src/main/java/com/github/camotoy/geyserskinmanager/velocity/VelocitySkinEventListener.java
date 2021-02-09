package com.github.camotoy.geyserskinmanager.velocity;

import com.github.camotoy.geyserskinmanager.common.RawSkin;
import com.github.camotoy.geyserskinmanager.common.SkinEntry;
import com.github.camotoy.geyserskinmanager.common.platform.ProxyPluginMessageSend;
import com.github.camotoy.geyserskinmanager.common.platform.SkinEventListener;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.util.GameProfile;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VelocitySkinEventListener extends SkinEventListener<Player, ServerConnection> implements ProxyPluginMessageSend<ServerConnection> {
    private final VelocityBedrockSkinUtilityListener capeListener;

    public VelocitySkinEventListener(ProxyServer server, GeyserSkinManager plugin, File skinDatabaseLocation, Logger logger) {
        super(skinDatabaseLocation, logger::warn);

        boolean useCapeListener = true;
        try {
            Class.forName("com.velocitypowered.api.event.player.PlayerChannelRegisterEvent");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warn("Please update Velocity in order to use the BedrockSkinUtility mod alongside this plugin!");
            useCapeListener = false;
        }

        if (useCapeListener) {
            this.capeListener = new VelocityBedrockSkinUtilityListener(server, this.database, this.skinRetriever);
            server.getEventManager().register(plugin, this.capeListener);
            server.getChannelRegistrar().register(VelocityConstants.MOD_PLUGIN_MESSAGE_NAME);
        } else {
            this.capeListener = null;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void onServerConnected(ServerPostConnectEvent event) {
        if (event.getPreviousServer() == null) {
            boolean shouldApply = true;
            for (GameProfile.Property property : event.getPlayer().getGameProfileProperties()) {
                if (property.getName().equals("textures")) {
                    // Don't overwrite existing textures
                    shouldApply = false;
                    break;
                }
            }

            RawSkin skin = null;
            if (shouldApply) {
                skin = this.skinRetriever.getBedrockSkin(event.getPlayer().getUniqueId());
                if (skin != null) {
                    uploadOrRetrieveSkin(event.getPlayer(), null, skin);
                }
            }

            if (this.capeListener != null) {
                if (skin != null || skinRetriever.isBedrockPlayer(event.getPlayer().getUniqueId())) {
                    this.capeListener.onBedrockPlayerJoin(event.getPlayer());
                }
            }
        }
    }

    @Override
    public void onSuccess(Player player, ServerConnection server, SkinEntry entry) {
        List<GameProfile.Property> properties = new ArrayList<>(player.getGameProfileProperties());
        properties.add(new GameProfile.Property("textures", entry.getJavaSkinValue(), entry.getJavaSkinSignature()));
        player.setGameProfileProperties(properties); // player.getGameProfileProperties() can be immutable

        player.getCurrentServer().ifPresent(serverConnection -> sendSkinToBackendServer(player.getUniqueId(), serverConnection, entry));
    }

    @Override
    public UUID getUUID(Player player) {
        return player.getUniqueId();
    }

    @Override
    public void sendPluginMessage(ServerConnection server, byte[] payload) {
        server.sendPluginMessage(VelocityConstants.SKIN_PLUGIN_MESSAGE_NAME, payload);
    }
}
