package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.SkinDatabase;
import com.github.camotoy.geyserskinmanager.common.platform.BedrockSkinUtilityListener;
import com.github.camotoy.geyserskinmanager.common.skinretriever.BedrockSkinRetriever;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

import java.util.UUID;

public class SpigotBedrockSkinUtilityListener extends BedrockSkinUtilityListener<Player> implements Listener {
    private final GeyserSkinManager plugin;

    public SpigotBedrockSkinUtilityListener(GeyserSkinManager plugin, SkinDatabase database, BedrockSkinRetriever skinRetriever) {
        super(database, skinRetriever);
        this.plugin = plugin;
    }

    @EventHandler
    public void onChannelRegistered(PlayerRegisterChannelEvent event) {
        if (event.getChannel().equals(Constants.MOD_PLUGIN_MESSAGE_NAME)) {
            onModdedPlayerConfirm(event.getPlayer());
        }
    }

    @Override
    public void sendCape(byte[] payload, Player player) {
        player.sendPluginMessage(plugin, Constants.MOD_PLUGIN_MESSAGE_NAME, payload);
    }

    @Override
    public UUID getUUID(Player player) {
        return player.getUniqueId();
    }
}
