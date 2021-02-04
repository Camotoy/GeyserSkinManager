package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.camotoy.geyserskinmanager.common.*;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import com.github.camotoy.geyserskinmanager.spigot.profile.MinecraftProfileWrapper;
import com.github.camotoy.geyserskinmanager.spigot.profile.PaperProfileWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class PaperEventListener extends EventListener {
    public PaperEventListener(GeyserSkinManager plugin, boolean bungeeCordMode) {
        super(plugin, bungeeCordMode);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerProfile playerProfile = event.getPlayer().getPlayerProfile();
        if (!playerProfile.hasTextures()) { // Don't add new textures if the player already has some. This behavior may change in the future.
            RawSkin skin = skinRetriever.getBedrockSkin(event.getPlayer().getUniqueId());
            if (skin != null) {
                MinecraftProfileWrapper profile = new PaperProfileWrapper(playerProfile);
                uploadOrRetrieveSkin(profile, event.getPlayer(), skin);
            }
        }
    }

    @Override
    public MinecraftProfileWrapper getMinecraftProfileWrapper(Player player) {
        return new PaperProfileWrapper(player.getPlayerProfile());
    }
}
