package com.github.camotoy.geyserskinmanager.common.platform;

import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.RawCape;
import com.github.camotoy.geyserskinmanager.common.SkinDatabase;
import com.github.camotoy.geyserskinmanager.common.skinretriever.BedrockSkinRetriever;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                DataOutputStream out = new DataOutputStream(byteArrayOutputStream);
                out.writeInt(Constants.CAPE_PLUGIN_MESSAGE_VERSION);
                out.writeLong(uuid.getMostSignificantBits());
                out.writeLong(uuid.getLeastSignificantBits());
                out.writeInt(cape.width);
                out.writeInt(cape.height);
                out.writeInt(cape.data.length);
                for (byte data : cape.data) {
                    out.writeByte(data);
                }

                capeData = byteArrayOutputStream.toByteArray();
                out.close();
            } catch (IOException e) {
                throw new RuntimeException("Could not write cape data!", e);
            }

            database.addCape(uuid, capeData);
            return capeData;
        }
        return null;
    }

}
