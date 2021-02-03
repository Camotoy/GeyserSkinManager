package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.github.camotoy.geyserskinmanager.common.RawSkin;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import com.github.camotoy.geyserskinmanager.spigot.profile.GameProfileWrapper;
import com.github.camotoy.geyserskinmanager.spigot.profile.MinecraftProfileWrapper;
import com.mojang.authlib.GameProfile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SpigotEventListener extends EventListener {
    private final Method getProfileMethod;

    public SpigotEventListener(GeyserSkinManager plugin) {
        super(plugin);
        String nmsVersion = plugin.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
            this.getProfileMethod = craftPlayerClass.getMethod("getProfile");
        } catch (Exception e) {
            throw new RuntimeException("getProfile method not found!", e);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        RawSkin skin = skinRetriever.getBedrockSkin(event.getPlayer().getUniqueId());
        if (skin != null) {
            GameProfile gameProfile;
            try {
                gameProfile = (GameProfile) getProfileMethod.invoke(event.getPlayer());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Could not find GameProfile for " + event.getPlayer().getName(), e);
            }

            if (!gameProfile.getProperties().containsKey("textures")) {
                MinecraftProfileWrapper profile = new GameProfileWrapper(gameProfile);
                uploadOrRetrieveSkin(profile, event.getPlayer(), skin);
            }
        }
    }
}
