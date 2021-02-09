package com.github.camotoy.geyserskinmanager.spigot.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GameProfileWrapper implements MinecraftProfileWrapper {
    private static final Method getProfileMethod;

    private final GameProfile profile;

    static {
        String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
            getProfileMethod = craftPlayerClass.getMethod("getProfile");
        } catch (Exception e) {
            throw new RuntimeException("getProfile method not found!", e);
        }
    }

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

    public static MinecraftProfileWrapper from(Player player) {
        return new GameProfileWrapper(getGameProfile(player));
    }

    public static GameProfile getGameProfile(Player player) {
        try {
            return (GameProfile) getProfileMethod.invoke(player);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not find GameProfile for " + player.getName(), e);
        }
    }
}
