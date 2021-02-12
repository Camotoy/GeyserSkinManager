package com.github.camotoy.geyserskinmanager.common.platform;

import com.github.camotoy.geyserskinmanager.common.*;
import com.github.camotoy.geyserskinmanager.common.skinretriever.BedrockSkinRetriever;
import org.geysermc.connector.utils.MathUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BedrockSkinUtilityListener<T> implements PlatformPlayerUuidSupport<T> {
    protected final Map<UUID, T> moddedPlayers = new ConcurrentHashMap<>();
    protected final SkinDatabase database;
    protected final BedrockSkinRetriever skinRetriever;

    public BedrockSkinUtilityListener(SkinDatabase database, BedrockSkinRetriever skinRetriever) {
        this.database = database;
        this.skinRetriever = skinRetriever;
    }

    public void sendAllCapes(T player) {
        for (Map.Entry<UUID, BedrockPluginMessageData> data : database.getPluginMessageData()) {
            sendPluginMessageData(player, data.getValue());
        }
    }

    public void sendPluginMessageData(T player, BedrockPluginMessageData data) {
        if (data.capeData != null) {
            sendPluginMessage(data.capeData, player);
        }

        if (data.skinInfo != null && data.skinData != null) {
            sendPluginMessage(data.skinInfo, player);
            for (byte[] skinData : data.skinData) {
                sendPluginMessage(skinData, player);
            }
        }
    }

    public abstract void sendPluginMessage(byte[] payload, T player);

    /**
     * @param player the player that we know has the mod installed.
     */
    public void onModdedPlayerConfirm(T player) {
        UUID uuid = getUUID(player);
        if (!moddedPlayers.containsKey(uuid)) {
            moddedPlayers.put(uuid, player);
            sendAllCapes(player);
        }
    }

    public void onBedrockPlayerJoin(T player, RawSkin skin) {
        BedrockPluginMessageData data = getSkinAndCape(getUUID(player), skin);
        if (data != null) {
            for (T moddedPlayer : moddedPlayers.values()) {
                sendPluginMessageData(moddedPlayer, data);
            }
        }
    }

    public void onPlayerLeave(T player) {
        UUID uuid = getUUID(player);
        database.removePluginMessageData(uuid);
        moddedPlayers.remove(uuid, player);
    }

    public BedrockPluginMessageData getSkinAndCape(UUID uuid, RawSkin skin) {
        BedrockPluginMessageData data = getSkin(uuid, skin, null);
        data = getCape(uuid, data);
        return data;
    }

    /**
     * Should be overwritten in any platform that limits the size of a plugin message.
     * Defaults to Minecraft 1.16's maximum.
     */
    public int getPluginMessageDataLimit() {
        return 1048576;
    }

    public BedrockPluginMessageData getSkin(UUID uuid, RawSkin skin, BedrockPluginMessageData pluginMessageData) {
        if (skin == null) {
            return null;
        }
        byte[] skinInfo;
        int chunkSize;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(byteArrayOutputStream)) {
            out.writeInt(BedrockSkinPluginMessageType.SKIN_INFORMATION.ordinal());
            out.writeInt(Constants.SKIN_DATA_PLUGIN_MESSAGE_TYPE_VERSION);

            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());

            out.writeInt(skin.width);
            out.writeInt(skin.height);

            boolean hasGeometry = skin.geometry != null && !skin.geometry.trim().equals("null");
            out.writeBoolean(hasGeometry);
            if (hasGeometry) {
                // Note that, because we won't be overriding the geometry, we shouldn't need to send if the player is alex or not
                // Because this plugin will send the player with that model.
                // The texture might still be HD, though.
                writeString(out, skin.geometry);
                writeString(out, skin.geometryName);
            }

            // Skin data chunk size
            chunkSize = (int) Math.ceil(skin.data.length / (double) getPluginMessageDataLimit());
            out.writeInt(chunkSize);

            skinInfo = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Could not write skin information!", e);
        }

        if (pluginMessageData == null) {
            pluginMessageData = new BedrockPluginMessageData();
            database.addPluginMessageData(uuid, pluginMessageData);
        }
        pluginMessageData.skinInfo = skinInfo;


        try (ByteArrayOutputStream headerBAOS = new ByteArrayOutputStream(); DataOutputStream headerOut = new DataOutputStream(headerBAOS)) {
            headerOut.writeInt(BedrockSkinPluginMessageType.SKIN_DATA.ordinal());
            // Use the player UUID as a common header
            headerOut.writeLong(uuid.getMostSignificantBits());
            headerOut.writeLong(uuid.getLeastSignificantBits());

            int headerSize = headerOut.size();

            int offset;
            byte[] currentSkinData;
            pluginMessageData.skinData = new byte[chunkSize][];

            for (int i = 0; i < pluginMessageData.skinData.length; i++) {
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(byteArrayOutputStream)) {
                    out.write(headerBAOS.toByteArray());
                    out.writeInt(i); // chunk index

                    offset = i * (getPluginMessageDataLimit() - headerSize - out.size());
                    currentSkinData = new byte[(int) MathUtils.constrain(skin.data.length - offset, 0, getPluginMessageDataLimit() - headerSize - out.size())];

                    try (InputStream stream = new ByteArrayInputStream(skin.data)) {
                        //noinspection ResultOfMethodCallIgnored
                        stream.skip(offset);
                        //noinspection ResultOfMethodCallIgnored
                        stream.read(currentSkinData, 0, currentSkinData.length);
                        byteArrayOutputStream.write(currentSkinData);
                    }
                    pluginMessageData.skinData[i] = byteArrayOutputStream.toByteArray();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not write skin information!", e);
        }

        return pluginMessageData;
    }

    public BedrockPluginMessageData getCape(UUID uuid, BedrockPluginMessageData pluginMessageData) {
        RawCape cape = this.skinRetriever.getBedrockCape(uuid);
        byte[] capeData;
        if (cape != null) {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(byteArrayOutputStream)) {
                out.writeInt(BedrockSkinPluginMessageType.CAPE.ordinal());
                out.writeInt(Constants.CAPE_PLUGIN_MESSAGE_TYPE_VERSION);

                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());

                out.writeInt(cape.width);
                out.writeInt(cape.height);

                writeString(out, cape.id);

                out.writeInt(cape.data.length);
                for (byte data : cape.data) {
                    out.writeByte(data);
                }

                capeData = byteArrayOutputStream.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Could not write cape data!", e);
            }

            if (pluginMessageData == null) {
                pluginMessageData = new BedrockPluginMessageData();
                database.addPluginMessageData(uuid, pluginMessageData);
            }
            pluginMessageData.capeData = capeData;
        }
        return pluginMessageData;
    }

    /**
     * Platform-agnostic way of writing a string.
     */
    protected void writeString(DataOutputStream out, String string) throws IOException {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        for (byte data : bytes) {
            out.writeByte(data);
        }
    }
}
