package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.github.camotoy.geyserskinmanager.common.*;
import com.github.camotoy.geyserskinmanager.common.skinretriever.BedrockSkinRetriever;
import com.github.camotoy.geyserskinmanager.common.skinretriever.GeyserSkinRetriever;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import com.github.camotoy.geyserskinmanager.spigot.profile.MinecraftProfileWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.UUID;

public abstract class SpigotPlatformEventListener implements Listener, PluginMessageListener {
    protected final SpigotBedrockSkinUtilityListener capeListener;
    protected final SkinDatabase database;
    protected final GeyserSkinManager plugin;
    protected final BedrockSkinRetriever skinRetriever;
    protected final SkinUploader skinUploader = new SkinUploader();
    private boolean useNewHidePlayerMethods;

    public SpigotPlatformEventListener(GeyserSkinManager plugin, boolean bungeeCordMode) {
        this.plugin = plugin;

        if (bungeeCordMode) {
            // BungeeCord takes care of the database, so we don't need to
            this.capeListener = null;
            this.database = null;
            this.skinRetriever = null;
        } else {
            this.database = new SkinDatabase(plugin.getDataFolder());
            this.skinRetriever = new GeyserSkinRetriever();
            this.capeListener = new SpigotBedrockSkinUtilityListener(this.plugin, this.database, this.skinRetriever);
            Bukkit.getMessenger().registerIncomingPluginChannel(this.plugin, Constants.BEDROCK_SKIN_UTILITY_INIT_NAME, this.capeListener);
        }

        try {
            Player.class.getMethod("hidePlayer", Plugin.class, Player.class);
            this.useNewHidePlayerMethods = true;
        } catch (NoSuchMethodException e) {
            this.useNewHidePlayerMethods = false;
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        this.capeListener.onPlayerLeave(event.getPlayer());
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
    
    public String[] RetrieveSkin(Player player, RawSkin skin) {
        PlayerEntry playerEntry = database.getPlayerEntry(player.getUniqueId());
        
        SkinEntry setSkin = null;
        int dateAdded = 0;
        for (SkinEntry skinEntry : playerEntry.getSkinEntries()) {
            if (dateAdded < skinEntry.dateAdded()) {
                dateAdded = skinEntry.dateAdded();
                setSkin = skinEntry;
            }
        }
        if (setSkin != null) {
            return {setSkin.javaSkinValue(), setSkin.javaSkinSignature()};
        }
        
        /*
            Default Steve Skin, Value and Signature
            This is Provided if setSkin is Null
            (Just to bypass any errors)
        */
        
        String value = "ewogICJ0aW1lc3RhbXAiIDogMTYxMjY0NzA0MzEwOCwKICAicHJvZmlsZUlkIiA6ICJmYmYyMmIyMjNjN2E0NzA0OGYwM2U0MzVhZGFhNGVhZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCZWFyZGVkU3RldmUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWE0YWY3MTg0NTVkNGFhYjUyOGU3YTYxZjg2ZmEyNWU2YTM2OWQxNzY4ZGNiMTNmN2RmMzE5YTcxM2ViODEwYiIKICAgIH0KICB9Cn0=";
        String signature = "Et91ODhR0JTM3jQvI3x9b0d1mdAw1O9iE8vx5M0UfaV+qiCpH0S2OjkyIAN+vRi7FK6DjIvAEng3Z6MZYa+BexicYRUJBstpPHI36jcoJMbZ/EHZcUok4bb4LRQLrYRTlMPGUZU7CwVXMCu33p60TR6tx9mSFIQFJiAbnK0Fj5+9y2toLXKn/pkntnJHR/fS6oImILHx/pTnPPLjG9ixSot4C9kmdcdTS5FKfui4kU8p0SWjwC3glqvGOslZngbReke8sduNvVUFiqRKELZnYCloAs0q6l+TQigcxwlidtV5fmbRg/kHTuHjCahaY2REYKJu0E7IWyLQh063KvhqplZLiAkhj1KsldAFDhwIAqR9M0eO9DDnUVFWv+gSlNLu23eHzKwwb2g/c3HchVyfo15qpVAgL+qzhzcrUumWCLR42ywMvzddH7L3o/cnJI5leQ6lQUb2CeVhPTTarnIXwti/Q1NO16gr/GN2an2Tpin/Ucc/zJsLVZwG0KnhWnmWUEs3sURcpEv3x02A95hMcTQbDIiZ71XvVW77i7nD9P9/DSY2WSfc2JpjGX0RKMKOBDr6tzz4ShAsJrWz+N2x3QEV7w9r/2paPgR9nnKBQeTYFkM8H43WhJjOmz9B4HR0zKn+gD/azUWmynEt+zEMZ1Lnlv3x/YHzfrkmPDmfQJY=";
        
        return {value, signature}
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
            SkinEntry skinEntry = new SkinEntry(skin.rawData, uploadResult.getResponse().get("value").getAsString(),
                    uploadResult.getResponse().get("signature").getAsString(), false);
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
            if (version != Constants.SKIN_PLUGIN_MESSAGE_VERSION) {
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
