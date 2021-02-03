package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.github.camotoy.geyserskinmanager.common.*;
import com.github.camotoy.geyserskinmanager.common.skinretriever.BedrockSkinRetriever;
import com.github.camotoy.geyserskinmanager.common.skinretriever.GeyserSkinRetriever;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import com.github.camotoy.geyserskinmanager.spigot.profile.MinecraftProfileWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public abstract class EventListener implements Listener, PluginMessageListener {
    protected final SkinDatabase database;
    protected final GeyserSkinManager plugin;
    protected final BedrockSkinRetriever skinRetriever;
    protected final SkinUploader skinUploader = new SkinUploader();
    private boolean useNewHidePlayerMethods;

    public EventListener(GeyserSkinManager plugin, boolean bungeeCordMode) {
        this.plugin = plugin;

        if (bungeeCordMode) {
            // BungeeCord takes care of the database, so we don't need to
            this.database = null;
            this.skinRetriever = null;
        } else {
            this.database = new SkinDatabase(plugin.getDataFolder());
            this.skinRetriever = new GeyserSkinRetriever();
        }

        try {
            Player.class.getMethod("hidePlayer", Plugin.class, Player.class);
            this.useNewHidePlayerMethods = true;
        } catch (NoSuchMethodException e) {
            this.useNewHidePlayerMethods = false;
        }
    }

    public void shutdown() {
        if (database != null) {
            database.clear();
        }
    }

    protected void uploadOrRetrieveSkin(MinecraftProfileWrapper profile, Player player, RawSkin skin) {
        PlayerEntry playerEntry = database.getPlayerEntry(player.getUniqueId());

        if (playerEntry == null) {
            // Fresh join
            uploadSkin(skin, profile, player, null);
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
                uploadSkin(skin, profile, player, playerEntry);
            } else {
                // We have the skin, we can go straight to applying it to the player
                setSkin(profile, player, setSkin);
            }
        }
    }

    protected void uploadSkin(RawSkin skin, MinecraftProfileWrapper profile, Player player, PlayerEntry playerEntry) {
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

            setSkin(profile, player, skinEntry);

            // Save the information so we don't have to upload skins to Mineskin again
            database.savePlayerInformation(playerEntryToSave);
        });
    }

    @Override
    public void onPluginMessageReceived(@Nonnull String channel, @Nonnull Player player, @Nonnull byte[] message) {
        if (!channel.equals(Constants.SKIN_PLUGIN_MESSAGE_NAME)) {
            return;
        }

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            int version = in.readInt();
            if (version != Constants.PLUGIN_MESSAGE_VERSION) {
                plugin.getLogger().warning("Received a plugin message with an invalid version! Make sure that GeyserSkinManager is updated on both BungeeCord and backend servers!");
                return;
            }
            Player bedrockPlayer = Bukkit.getPlayer(new UUID(in.readLong(), in.readLong()));
            String value = in.readUTF();
            String signature = in.readUTF();
            SkinEntry skinEntry = new SkinEntry(value, signature);
            setSkin(getMinecraftProfileWrapper(bedrockPlayer), bedrockPlayer, skinEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract MinecraftProfileWrapper getMinecraftProfileWrapper(Player player);

    protected void setSkin(MinecraftProfileWrapper profile, Player player, SkinEntry skinEntry) {
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
    protected void hidePlayer(Plugin plugin, Player sourcePlayer, Player hiddenPlayer) {
        if (useNewHidePlayerMethods) {
            sourcePlayer.hidePlayer(plugin, hiddenPlayer);
        } else {
            sourcePlayer.hidePlayer(hiddenPlayer);
        }
    }

    @SuppressWarnings("deprecation")
    protected void showPlayer(Plugin plugin, Player sourcePlayer, Player hiddenPlayer) {
        if (useNewHidePlayerMethods) {
            sourcePlayer.showPlayer(plugin, hiddenPlayer);
        } else {
            sourcePlayer.showPlayer(hiddenPlayer);
        }
    }
}
