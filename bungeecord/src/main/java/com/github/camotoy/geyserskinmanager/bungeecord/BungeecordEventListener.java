package com.github.camotoy.geyserskinmanager.bungeecord;

import com.github.camotoy.geyserskinmanager.common.*;
import com.github.camotoy.geyserskinmanager.common.skinretriever.BedrockSkinRetriever;
import com.github.camotoy.geyserskinmanager.common.skinretriever.GeyserSkinRetriever;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BungeecordEventListener implements Listener {
    private final BedrockSkinRetriever skinRetriever;
    private final SkinDatabase database;
    private final GeyserSkinManager plugin;
    private final SkinUploader skinUploader = new SkinUploader();

    public BungeecordEventListener(GeyserSkinManager plugin) {
        this.database = new SkinDatabase(plugin.getDataFolder());
        this.plugin = plugin;
        this.skinRetriever = new GeyserSkinRetriever();
    }

    public void shutdown() {
        this.database.clear();
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        if (event.getPlayer().getUniqueId().version() == 4) {
            // Linked player that probably already has a skin, or an online mode player
            return;
        }

        RawSkin skin = this.skinRetriever.getBedrockSkin(event.getPlayer().getUniqueId());
        if (skin != null) {
            uploadOrRetrieveSkin(event.getPlayer(), event.getServer(), skin);
        }
    }

    protected void uploadOrRetrieveSkin(ProxiedPlayer player, Server server, RawSkin skin) {
        PlayerEntry playerEntry = database.getPlayerEntry(player.getUniqueId());

        if (playerEntry == null) {
            // Fresh join
            uploadSkin(skin, player, server,null);
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
                uploadSkin(skin, player, server, playerEntry);
            } else {
                // We have the skin, we can go straight to applying it to the player
                sendSkin(player, server, setSkin);
            }
        }
    }

    protected void uploadSkin(RawSkin skin, ProxiedPlayer player, Server server, PlayerEntry playerEntry) {
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

            sendSkin(player, server, skinEntry);

            // Save the information so we don't have to upload skins to Mineskin again
            database.savePlayerInformation(playerEntryToSave);
        });
    }

    protected void sendSkin(ProxiedPlayer player, Server server, SkinEntry skinEntry) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
            out.writeInt(Constants.PLUGIN_MESSAGE_VERSION); // Ensure that both plugins are up-to-date
            out.writeLong(player.getUniqueId().getMostSignificantBits());
            out.writeLong(player.getUniqueId().getLeastSignificantBits());
            out.writeUTF(skinEntry.getJavaSkinValue());
            out.writeUTF(skinEntry.getJavaSkinSignature());

            server.sendData(Constants.SKIN_PLUGIN_MESSAGE_NAME, byteArrayOutputStream.toByteArray());

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
