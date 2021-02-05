package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.SkinDatabase;
import com.github.camotoy.geyserskinmanager.common.platform.BedrockSkinUtilityListener;
import com.github.camotoy.geyserskinmanager.common.skinretriever.BedrockSkinRetriever;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class SpigotBedrockSkinUtilityListener extends BedrockSkinUtilityListener<Player> implements PluginMessageListener {
    private Method addChannelMethod = null;

    private final GeyserSkinManager plugin;

    public SpigotBedrockSkinUtilityListener(GeyserSkinManager plugin, SkinDatabase database, BedrockSkinRetriever skinRetriever) {
        super(database, skinRetriever);
        this.plugin = plugin;
    }

    public void addPluginMessageChannel(Player player, String channelName) {
        // plz
        if (this.addChannelMethod == null) {
            try {
                this.addChannelMethod = player.getClass().getMethod("addChannel", String.class);
            } catch (Exception e) {
                throw new RuntimeException("Could not find the channel field for player!" + e);
            }
        }
        try {
            this.addChannelMethod.invoke(player, channelName);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPluginMessageReceived(@Nonnull String channel, @Nonnull Player player, @Nonnull byte[] message) {
        if (!channel.equals(Constants.BEDROCK_SKIN_UTILITY_INIT_NAME)) {
            return;
        }

        if (!moddedPlayers.containsKey(player.getUniqueId())) {
            moddedPlayers.put(player.getUniqueId(), player);

            addPluginMessageChannel(player, Constants.CAPE_PLUGIN_MESSAGE_NAME);

            sendAllCapes(player);
        }
    }

    @Override
    public void sendCape(byte[] payload, Player player) {
        player.sendPluginMessage(plugin, Constants.CAPE_PLUGIN_MESSAGE_NAME, payload);
    }

    @Override
    public UUID getUUID(Player player) {
        return player.getUniqueId();
    }
}
