package com.github.camotoy.geyserskinmanager.common;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SkinEntry {
    private final String bedrockBase64Skin;
    private final String javaSkinValue;
    private final String javaSkinSignature;
    private final long dateAdded;

    public SkinEntry(String bedrockSkin, String javaSkinValue, String javaSkinSignature, boolean isBedrockSkinEncoded) {
        this(bedrockSkin, javaSkinValue, javaSkinSignature, isBedrockSkinEncoded, System.currentTimeMillis());
    }

    public SkinEntry(String bedrockSkin, String javaSkinValue, String javaSkinSignature, boolean isBedrockSkinEncoded, long dateAdded) {
        this.bedrockBase64Skin = isBedrockSkinEncoded ? bedrockSkin : Base64.getEncoder().encodeToString(bedrockSkin.getBytes(StandardCharsets.UTF_8));
        this.javaSkinValue = javaSkinValue;
        this.javaSkinSignature = javaSkinSignature;
        this.dateAdded = dateAdded;
    }

    public SkinEntry(String javaSkinValue, String javaSkinSignature) {
        this.bedrockBase64Skin = "";
        this.javaSkinValue = javaSkinValue;
        this.javaSkinSignature = javaSkinSignature;
        this.dateAdded = 0; // Not going to be saved (no Bedrock texture), so it does not matter
    }

    public String getBedrockBase64Skin() {
        return bedrockBase64Skin;
    }

    public String getBedrockSkin() {
        return new String(Base64.getDecoder().decode(bedrockBase64Skin), StandardCharsets.UTF_8);
    }

    public String getJavaSkinValue() {
        return javaSkinValue;
    }

    public String getJavaSkinSignature() {
        return javaSkinSignature;
    }

    public long getDateAdded() {
        return dateAdded;
    }
}
