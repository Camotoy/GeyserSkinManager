package com.github.camotoy.geyserskinmanager.spigot;

import com.github.camotoy.geyserskinmanager.common.SkinEntry;
import com.github.camotoy.geyserskinmanager.spigot.profile.MinecraftProfileWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SpigotSkinApplier {
    private final GeyserSkinManager plugin;
    private boolean useNewHidePlayerMethods;

    public SpigotSkinApplier(GeyserSkinManager plugin) {
        this.plugin = plugin;

        try {
            Player.class.getMethod("hidePlayer", Plugin.class, Player.class);
            this.useNewHidePlayerMethods = true;
        } catch (NoSuchMethodException e) {
            this.useNewHidePlayerMethods = false;
        }
    }
    public void setSkin(MinecraftProfileWrapper profile, Player player, SkinEntry skinEntry) {
        profile.applyTexture(skinEntry.getJavaSkinValue(), skinEntry.getJavaSkinSignature());

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            profile.setPlayerProfile(player);

            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                if (!otherPlayer.equals(player) && otherPlayer.canSee(player)) {
                    hidePlayer(plugin, otherPlayer, player);
                    showPlayer(plugin, otherPlayer, player);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void hidePlayer(Plugin plugin, Player sourcePlayer, Player hiddenPlayer) {
        if (useNewHidePlayerMethods) {
            sourcePlayer.hidePlayer(plugin, hiddenPlayer);
        } else {
            sourcePlayer.hidePlayer(hiddenPlayer);
        }
    }

    @SuppressWarnings("deprecation")
    private void showPlayer(Plugin plugin, Player sourcePlayer, Player hiddenPlayer) {
        if (useNewHidePlayerMethods) {
            sourcePlayer.showPlayer(plugin, hiddenPlayer);
        } else {
            sourcePlayer.showPlayer(hiddenPlayer);
        }
    }
}
