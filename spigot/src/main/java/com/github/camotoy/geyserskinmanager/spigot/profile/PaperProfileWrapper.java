package com.github.camotoy.geyserskinmanager.spigot.profile;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.entity.Player;

public class PaperProfileWrapper implements MinecraftProfileWrapper {
    private final PlayerProfile profile;

    public PaperProfileWrapper(PlayerProfile profile) {
        this.profile = profile;
    }

    @Override
    public void applyTexture(String value, String signature) {
        profile.setProperty(new ProfileProperty("textures", value, signature));
    }

    @Override
    public void setPlayerProfile(Player player) {
        // The profile we get is a clone and not the original
        player.setPlayerProfile(this.profile);
    }

    public static MinecraftProfileWrapper from(Player player) {
        return new PaperProfileWrapper(player.getPlayerProfile());
    }
}
