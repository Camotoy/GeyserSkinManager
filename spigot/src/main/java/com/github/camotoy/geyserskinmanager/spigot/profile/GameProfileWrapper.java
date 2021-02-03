package com.github.camotoy.geyserskinmanager.spigot.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.entity.Player;

public class GameProfileWrapper implements MinecraftProfileWrapper {
    private final GameProfile profile;

    public GameProfileWrapper(GameProfile profile) {
        this.profile = profile;
    }

    @Override
    public void applyTexture(String value, String signature) {
        this.profile.getProperties().put("textures", new Property("textures", value, signature));
    }

    @Override
    public void setPlayerProfile(Player player) {
        // This is not a clone; no work needed
    }
}
