package com.github.camotoy.geyserskinmanager.common.skinretriever;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.camotoy.geyserskinmanager.common.RawCape;
import com.github.camotoy.geyserskinmanager.common.RawSkin;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.auth.BedrockClientData;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class GeyserSkinRetriever implements BedrockSkinRetriever {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public RawCape getBedrockCape(UUID uuid) {
        GeyserSession session = GeyserImpl.getInstance().connectionByUuid(uuid);
        if (session == null) {
            return null;
        }

        if (session.getClientData().getCapeImageWidth() == 0 || session.getClientData().getCapeImageHeight() == 0 ||
                session.getClientData().getCapeData().length == 0) {
            return null;
        }
        return new RawCape(session.getClientData().getCapeImageWidth(), session.getClientData().getCapeImageHeight(),
                session.getClientData().getCapeId(), session.getClientData().getCapeData());
    }

    @Override
    public RawSkin getBedrockSkin(String name) {
        GeyserSession session = null;
        for (GeyserSession otherSession : GeyserImpl.getInstance().getSessionManager().getSessions().values()) {
            if (name.equals(otherSession.name())) {
                session = otherSession;
                break;
            }
        }
        if (session == null) {
            return null;
        }

        return getImage(session.getClientData());
    }

    @Override
    public RawSkin getBedrockSkin(UUID uuid) {
        GeyserSession session = GeyserImpl.getInstance().connectionByUuid(uuid);
        if (session == null) {
            return null;
        }

        return getImage(session.getClientData());
    }

    @Override
    public boolean isBedrockPlayer(UUID uuid) {
        return GeyserImpl.getInstance().connectionByUuid(uuid) != null;
    }

    /**
     * Taken from https://github.com/NukkitX/Nukkit/blob/master/src/main/java/cn/nukkit/network/protocol/LoginPacket.java
     */
    private RawSkin getImage(BedrockClientData clientData) {
        byte[] image = Base64.getDecoder().decode(clientData.getSkinData());
        if (image.length > (128 * 128 * 4) || clientData.isPersonaSkin()) {
            //System.out.println("Persona skins are not yet supported, sorry!");
            return null;
        }
        String geometryName = new String(Base64.getDecoder().decode(clientData.getGeometryName()), StandardCharsets.UTF_8);
        boolean alex = isAlex(geometryName);
        return new RawSkin(
                clientData.getSkinImageWidth(),
                clientData.getSkinImageHeight(),
                image, alex, geometryName,
                new String(Base64.getDecoder().decode(clientData.getGeometryData()), StandardCharsets.UTF_8),
                clientData.getSkinData()
        );
    }

    private boolean isAlex(String geometryName) {
        try {
            String defaultGeometryName = OBJECT_MAPPER.readTree(geometryName).get("geometry").get("default").asText();
            return "geometry.humanoid.customSlim".equals(defaultGeometryName);
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }
}
