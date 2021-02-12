package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.camotoy.geyserskinmanager.common.RawSkin;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import com.github.camotoy.geyserskinmanager.spigot.profile.MinecraftProfileWrapper;
import com.github.camotoy.geyserskinmanager.spigot.profile.PaperProfileWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class PaperEventListener extends SpigotPlatformEventListener {
    public PaperEventListener(GeyserSkinManager plugin, boolean bungeeCordMode) {
        super(plugin, bungeeCordMode);
    }

    @Override
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        RawSkin skin = null;
        PlayerProfile playerProfile = event.getPlayer().getPlayerProfile();
        if (!playerProfile.hasTextures()) { // Don't add new textures if the player already has some. This behavior may change in the future.
            skin = skinRetriever.getBedrockSkin(event.getPlayer().getUniqueId());
            if (skin != null) {
                MinecraftProfileWrapper profile = new PaperProfileWrapper(playerProfile);
                uploadOrRetrieveSkin(event.getPlayer(), profile, skin);
            }
        }

        if (skin != null || skinRetriever.isBedrockPlayer(event.getPlayer().getUniqueId())) {
            // Send cape even if the player has a skin or the skin cannot be sent
            modListener.onBedrockPlayerJoin(event.getPlayer(), skin);
        }
    }
}
