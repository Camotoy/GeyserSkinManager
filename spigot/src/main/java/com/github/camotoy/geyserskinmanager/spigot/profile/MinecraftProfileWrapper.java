package com.github.camotoy.geyserskinmanager.spigot.profile;

import org.bukkit.entity.Player;

/**
 * A wrapper around Minecraft game profiles; differs depending on the platform and version we're running.
 */
public interface MinecraftProfileWrapper {
    void applyTexture(String value, String signature);

    void setPlayerProfile(Player player);
}
