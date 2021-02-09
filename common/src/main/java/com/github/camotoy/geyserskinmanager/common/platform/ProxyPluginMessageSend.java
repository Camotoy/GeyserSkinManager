package com.github.camotoy.geyserskinmanager.common.platform;

import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.SkinEntry;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @param <S> server to send plugin message to
 */
public interface ProxyPluginMessageSend<S> {
    void sendPluginMessage(S server, byte[] payload);

    default void sendSkinToBackendServer(UUID uuid, S server, SkinEntry skinEntry) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(byteArrayOutputStream)) {
            out.writeInt(Constants.SKIN_PLUGIN_MESSAGE_VERSION); // Ensure that both plugins are up-to-date
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
            out.writeUTF(skinEntry.getJavaSkinValue());
            out.writeUTF(skinEntry.getJavaSkinSignature());

            sendPluginMessage(server, byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
