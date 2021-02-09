package com.github.camotoy.geyserskinmanager.spigot.listener;

import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.SkinEntry;
import com.github.camotoy.geyserskinmanager.spigot.GeyserSkinManager;
import com.github.camotoy.geyserskinmanager.spigot.SpigotSkinApplier;
import com.github.camotoy.geyserskinmanager.spigot.profile.GameProfileWrapper;
import com.github.camotoy.geyserskinmanager.spigot.profile.MinecraftProfileWrapper;
import com.github.camotoy.geyserskinmanager.spigot.profile.PaperProfileWrapper;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

public class BungeecordPluginMessageListener implements PluginMessageListener {
    private final GeyserSkinManager plugin;
    private final Function<Player, MinecraftProfileWrapper> getProfileFunction;
    private final SpigotSkinApplier skinApplier;

    public BungeecordPluginMessageListener(GeyserSkinManager plugin) {
        this.plugin = plugin;
        this.skinApplier = new SpigotSkinApplier(plugin);
        this.getProfileFunction = (PaperLib.isPaper() && PaperLib.isVersion(12, 2)) ?
                 PaperProfileWrapper::from : GameProfileWrapper::from;
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
            UUID uuid = new UUID(in.readLong(), in.readLong());
            Player bedrockPlayer = Bukkit.getPlayer(uuid);
            if (bedrockPlayer == null) {
                this.plugin.getLogger().warning("Player with UUID " + uuid + " could not be found!");
                return;
            }
            String value = in.readUTF();
            String signature = in.readUTF();
            SkinEntry skinEntry = new SkinEntry(value, signature);
            skinApplier.setSkin(getProfileFunction.apply(bedrockPlayer), bedrockPlayer, skinEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
