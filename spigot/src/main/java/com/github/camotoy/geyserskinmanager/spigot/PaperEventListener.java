package com.github.camotoy.geyserskinmanager.spigot;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.camotoy.geyserskinmanager.common.*;
import com.github.camotoy.geyserskinmanager.common.skinretriever.BedrockSkinRetriever;
import com.github.camotoy.geyserskinmanager.common.skinretriever.GeyserSkinRetriever;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class PaperEventListener implements EventListener {
    private final SkinDatabase database;
    private final GeyserSkinManager plugin;
    private final BedrockSkinRetriever skinRetriever = new GeyserSkinRetriever();
    private final SkinUploader skinUploader = new SkinUploader();

    public PaperEventListener(GeyserSkinManager plugin) {
        this.plugin = plugin;
        this.database = new SkinDatabase(plugin.getDataFolder());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerProfile profile = event.getPlayer().getPlayerProfile();
        if (!profile.hasTextures()) { // Don't add new textures if the player already has some. This behavior may change in the future.
            RawSkin skin = skinRetriever.getBedrockSkin(event.getPlayer().getUniqueId());
            if (skin != null) {
                PlayerEntry playerEntry = database.getPlayerEntry(event.getPlayer().getUniqueId());

                if (playerEntry == null) {
                    // Fresh join
                    uploadSkin(skin, profile, event.getPlayer(), null);
                } else {
                    // This player has joined before
                    SkinEntry setSkin = null;
                    for (SkinEntry skinEntry : playerEntry.getSkinEntries()) {
                        if (skinEntry.getBedrockSkin().equals(skin.rawData)) {
                            setSkin = skinEntry;
                            break;
                        }
                    }
                    if (setSkin == null) {
                        uploadSkin(skin, profile, event.getPlayer(), playerEntry);
                    } else {
                        // We have the skin, we can go straight to applying it to the player
                        setSkin(profile, event.getPlayer(), setSkin);
                    }
                }
            }
        }
    }

    private void uploadSkin(RawSkin skin, PlayerProfile profile, Player player, PlayerEntry playerEntry) {
        skinUploader.uploadSkin(skin).whenComplete((uploadResult, throwable) -> {
            if (!skinUploader.checkResult(plugin.getLogger(), player.getName(), uploadResult, throwable)) {
                return;
            }

            PlayerEntry playerEntryToSave;
            if (playerEntry == null) {
                playerEntryToSave = new PlayerEntry(player.getUniqueId());
            } else {
                playerEntryToSave = playerEntry;
            }
            SkinEntry skinEntry = new SkinEntry(skin.rawData, uploadResult.getResponse().get("value").getAsString(), uploadResult.getResponse().get("signature").getAsString(), false);
            playerEntryToSave.getSkinEntries().add(skinEntry);

            try {
                setSkin(profile, player, skinEntry);

                // Save the information so we don't have to upload skins to Mineskin again
                database.savePlayerInformation(playerEntryToSave);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setSkin(PlayerProfile profile, Player player, SkinEntry skinEntry) {
        profile.setProperty(new ProfileProperty("textures", skinEntry.getJavaSkinValue(), skinEntry.getJavaSkinSignature()));

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.setPlayerProfile(profile);

            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                if (!otherPlayer.equals(player) && otherPlayer.canSee(player)) {
                    otherPlayer.hidePlayer(plugin, player);
                    otherPlayer.showPlayer(plugin, player);
                }
            }
        });
    }
}
