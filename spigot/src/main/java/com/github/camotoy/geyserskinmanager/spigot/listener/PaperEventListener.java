package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.RawSkin;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import com.github.camotoy.geyserskinmanager.spigot.profile.MinecraftProfileWrapper;
import com.github.camotoy.geyserskinmanager.spigot.profile.PaperProfileWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public class PaperEventListener extends SpigotPlatformEventListener {
    public PaperEventListener(GeyserSkinManager plugin, boolean showSkins) {
        super(plugin, showSkins);
    }

    @Override
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        RawSkin skin = null;
        if (this.skinApplier != null) {
            PlayerProfile playerProfile = event.getPlayer().getPlayerProfile();
            if (hasNoTextures(playerProfile)) { // Don't add new textures if the player already has some. This behavior may change in the future.
                skin = skinRetriever.getBedrockSkin(event.getPlayer().getUniqueId());
                if (skin != null) {
                    MinecraftProfileWrapper profile = new PaperProfileWrapper(playerProfile);
                    uploadOrRetrieveSkin(event.getPlayer(), profile, skin);
                }
            }
        } else {
            skin = skinRetriever.getBedrockSkin(event.getPlayer().getUniqueId());
        }

        if (skin != null || skinRetriever.isBedrockPlayer(event.getPlayer().getUniqueId())) {
            // Send cape even if the player has a skin or the skin cannot be sent
            modListener.onBedrockPlayerJoin(event.getPlayer(), skin);
        }
    }

    private boolean hasNoTextures(final PlayerProfile playerProfile) {
        final Optional<ProfileProperty> property = playerProfile.getProperties()
                .stream()
                .filter(aProperty -> aProperty.getName().equals("textures"))
                .findFirst();
        return property.map(profileProperty -> Constants.FLOODGATE_STEVE_SKIN.equals(profileProperty.getValue())).orElse(true);
    }
}
