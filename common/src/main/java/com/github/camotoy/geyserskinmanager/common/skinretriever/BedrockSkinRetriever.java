package com.github.camotoy.geyserskinmanager.common.skinretriever;

import com.github.camotoy.geyserskinmanager.common.RawCape;
import com.github.camotoy.geyserskinmanager.common.RawSkin;

import java.util.UUID;

public interface BedrockSkinRetriever {
    RawCape getBedrockCape(UUID uuid);

    RawSkin getBedrockSkin(String name);

    RawSkin getBedrockSkin(UUID uuid);

    boolean isBedrockPlayer(UUID uuid);
}
