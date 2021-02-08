package com.github.camotoy.geyserskinmanager.common.skinretriever;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.camotoy.geyserskinmanager.common.RawCape;
import com.github.camotoy.geyserskinmanager.common.RawSkin;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.session.auth.BedrockClientData;
import org.geysermc.connector.skin.SkinProvider;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class GeyserSkinRetriever implements BedrockSkinRetriever {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public RawCape getBedrockCape(UUID uuid) {
        GeyserSession session = GeyserConnector.getInstance().getPlayerByUuid(uuid);
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
        for (GeyserSession otherSession : GeyserConnector.getInstance().getPlayers()) {
            if (name.equals(otherSession.getName())) {
                session = otherSession;
                break;
            }
        }
        if (session == null) {
            return null;
        }

        return getAndTransformImage(session.getClientData());
    }

    @Override
    public RawSkin getBedrockSkin(UUID uuid) {
        GeyserSession session = GeyserConnector.getInstance().getPlayerByUuid(uuid);
        if (session == null) {
            return null;
        }

        return getAndTransformImage(session.getClientData());
    }

    @Override
    public boolean isBedrockPlayer(UUID uuid) {
        return GeyserConnector.getInstance().getPlayerByUuid(uuid) != null;
    }

    /**
     * Taken from https://github.com/NukkitX/Nukkit/blob/master/src/main/java/cn/nukkit/network/protocol/LoginPacket.java
     */
    private RawSkin getImage(BedrockClientData clientData) {
        byte[] image = Base64.getDecoder().decode(clientData.getSkinData());
        if (image.length > (128 * 128 * 4) || clientData.isPersonaSkin()) {
            System.out.println("Persona skins are not yet supported, sorry!");
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

    private RawSkin getAndTransformImage(BedrockClientData clientData) {
        RawSkin skin = getImage(clientData);
        if (skin == null) {
            return null;
        }
        if (skin.width > 64 || skin.height > 64) {
            BufferedImage scaledImage = SkinProvider.imageDataToBufferedImage(skin.data, skin.width, skin.height);

            int max = Math.max(skin.width, skin.height);
            while (max > 64) {
                max /= 2;
                scaledImage = scale(scaledImage);
            }

            byte[] skinData = SkinProvider.bufferedImageToImageData(scaledImage);
            skin.width = scaledImage.getWidth();
            skin.height = scaledImage.getHeight();
            skin.data = skinData;
        }
        return skin;
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

    private BufferedImage scale(BufferedImage bufferedImage) {
        BufferedImage resized = new BufferedImage(bufferedImage.getWidth() / 2, bufferedImage.getHeight() / 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(bufferedImage, 0, 0, bufferedImage.getWidth() / 2, bufferedImage.getHeight() / 2, null);
        g2.dispose();
        return resized;
    }
}
