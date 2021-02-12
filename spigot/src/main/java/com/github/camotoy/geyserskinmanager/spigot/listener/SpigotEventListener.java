package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.github.camotoy.geyserskinmanager.common.RawSkin;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import com.github.camotoy.geyserskinmanager.spigot.profile.GameProfileWrapper;
import com.github.camotoy.geyserskinmanager.spigot.profile.MinecraftProfileWrapper;
import com.mojang.authlib.GameProfile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class SpigotEventListener extends SpigotPlatformEventListener {

    public SpigotEventListener(GeyserSkinManager plugin, boolean bungeeCordMode) {
        super(plugin, bungeeCordMode);
    }

    @Override
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        RawSkin skin = skinRetriever.getBedrockSkin(event.getPlayer().getUniqueId());
        if (skin != null) {
            GameProfile gameProfile = GameProfileWrapper.getGameProfile(event.getPlayer());

            if (!gameProfile.getProperties().containsKey("textures")) {
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
