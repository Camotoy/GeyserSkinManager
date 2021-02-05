package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.RawSkin;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import com.github.camotoy.geyserskinmanager.spigot.profile.GameProfileWrapper;
import com.github.camotoy.geyserskinmanager.spigot.profile.MinecraftProfileWrapper;
import com.mojang.authlib.GameProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SpigotEventListener extends SpigotPlatformEventListener {
    private final Method getProfileMethod;

    public SpigotEventListener(GeyserSkinManager plugin, boolean bungeeCordMode) {
        super(plugin, bungeeCordMode);
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
            GameProfile gameProfile = getGameProfile(event.getPlayer());

            if (!gameProfile.getProperties().containsKey("textures")) {
                MinecraftProfileWrapper profile = new GameProfileWrapper(gameProfile);
                uploadOrRetrieveSkin(profile, event.getPlayer(), skin);
            }
        }
        capeListener.addPluginMessageChannel(event.getPlayer(), Constants.INIT_PLUGIN_MESSAGE_NAME);
        event.getPlayer().sendPluginMessage(plugin, Constants.INIT_PLUGIN_MESSAGE_NAME, new byte[0]);

        if (skin != null || skinRetriever.isBedrockPlayer(event.getPlayer().getUniqueId())) {
            // Send cape even if the player has a skin or the skin cannot be sent
            capeListener.onBedrockPlayerJoin(event.getPlayer());
        }
    }

    @Override
    public MinecraftProfileWrapper getMinecraftProfileWrapper(Player player) {
        return new GameProfileWrapper(getGameProfile(player));
    }

    private GameProfile getGameProfile(Player player) {
        try {
            return (GameProfile) getProfileMethod.invoke(player);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not find GameProfile for " + player.getName(), e);
        }
    }
}
