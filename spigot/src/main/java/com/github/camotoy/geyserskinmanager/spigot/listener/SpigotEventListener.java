package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.github.camotoy.geyserskinmanager.common.RawSkin;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import com.github.camotoy.geyserskinmanager.spigot.profile.GameProfileWrapper;
import com.github.camotoy.geyserskinmanager.spigot.profile.MinecraftProfileWrapper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collection;

public class SpigotEventListener extends SpigotPlatformEventListener {

    public SpigotEventListener(GeyserSkinManager plugin, boolean showSkins) {
        super(plugin, showSkins);
    }

    @Override
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        RawSkin skin = skinRetriever.getBedrockSkin(event.getPlayer().getUniqueId());
        if (skin != null && this.skinApplier != null) {
            GameProfile gameProfile = GameProfileWrapper.getGameProfile(event.getPlayer());
            Collection<Property> properties = gameProfile.getProperties().get("textures");

            if (properties == null || properties.stream().noneMatch((property -> property.getValue().isEmpty()))) {
                MinecraftProfileWrapper profile = new GameProfileWrapper(gameProfile);
                uploadOrRetrieveSkin(event.getPlayer(), profile, skin);
            }
        }

        if (skin != null || skinRetriever.isBedrockPlayer(event.getPlayer().getUniqueId())) {
            // Send cape even if the player has a skin or the skin cannot be sent
            modListener.onBedrockPlayerJoin(event.getPlayer(), skin);
        }
    }
}
