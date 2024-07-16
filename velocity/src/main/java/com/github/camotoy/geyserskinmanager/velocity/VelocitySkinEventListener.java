package com.github.camotoy.geyserskinmanager.velocity;

import com.github.camotoy.geyserskinmanager.common.Constants;
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VelocitySkinEventListener extends SkinEventListener<Player, ServerConnection> implements ProxyPluginMessageSend<ServerConnection> {
    private final boolean showSkins;
    private final VelocityBedrockSkinUtilityListener modListener;

    public VelocitySkinEventListener(ProxyServer server, GeyserSkinManager plugin, Path skinDatabaseLocation, Logger logger, boolean showSkins) {
        super(skinDatabaseLocation, logger::warn);
        this.showSkins = showSkins;

        this.modListener = new VelocityBedrockSkinUtilityListener(server, this.database, this.skinRetriever);
        server.getEventManager().register(plugin, this.modListener);
        server.getChannelRegistrar().register(VelocityConstants.MOD_PLUGIN_MESSAGE_NAME);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void onServerConnected(ServerPostConnectEvent event) {
        if (event.getPreviousServer() == null) {
            boolean shouldApply = true;
            if (showSkins) {
                for (GameProfile.Property property : event.getPlayer().getGameProfileProperties()) {
                    if (property.getName().equals("textures") && (!property.getValue().isEmpty() && !property.getValue().equals(Constants.FLOODGATE_STEVE_SKIN))) {
                        // Don't overwrite existing textures
                        shouldApply = false;
                        break;
                    }
                }
            }

            RawSkin skin = null;
            if (shouldApply) {
                skin = this.skinRetriever.getBedrockSkin(event.getPlayer().getUniqueId());
                if (skin != null && showSkins) {
                    uploadOrRetrieveSkin(event.getPlayer(), null, skin);
                }
            }

            if (skin != null || skinRetriever.isBedrockPlayer(event.getPlayer().getUniqueId())) {
                this.modListener.onBedrockPlayerJoin(event.getPlayer(), skin);
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
