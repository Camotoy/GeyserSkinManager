package com.github.camotoy.geyserskinmanager.common.platform;

import com.github.camotoy.geyserskinmanager.common.BedrockSkinPluginMessageType;
import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.RawCape;
import com.github.camotoy.geyserskinmanager.common.SkinDatabase;
import com.github.camotoy.geyserskinmanager.common.skinretriever.BedrockSkinRetriever;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
        for (Map.Entry<UUID, byte[]> cape : database.getCapes()) {
            sendCape(cape.getValue(), player);
        }
    }

    public abstract void sendCape(byte[] payload, T player);

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

    public void onBedrockPlayerJoin(T player) {
        byte[] payload = getCape(getUUID(player));
        if (payload != null) {
            for (T moddedPlayer : moddedPlayers.values()) {
                sendCape(payload, moddedPlayer);
            }
        }
    }

    public void onPlayerLeave(T player) {
        UUID uuid = getUUID(player);
        database.removeCape(uuid);
        moddedPlayers.remove(uuid, player);
    }

    public byte[] getCape(UUID uuid) {
        RawCape cape = this.skinRetriever.getBedrockCape(uuid);
        byte[] capeData;
        if (cape != null) {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(byteArrayOutputStream)) {
                out.writeInt(BedrockSkinPluginMessageType.SEND_CAPE.ordinal());
                out.writeInt(Constants.CAPE_PLUGIN_MESSAGE_TYPE_VERSION);

                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());

                out.writeInt(cape.width);
                out.writeInt(cape.height);

                byte[] capeIdBytes = cape.id.getBytes(StandardCharsets.UTF_8);
                out.writeInt(capeIdBytes.length);
                for (byte data : capeIdBytes) {
                    out.writeByte(data);
                }

                out.writeInt(cape.data.length);
                for (byte data : cape.data) {
                    out.writeByte(data);
                }

                capeData = byteArrayOutputStream.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Could not write cape data!", e);
            }

            database.addCape(uuid, capeData);
            return capeData;
        }
        return null;
    }

}
