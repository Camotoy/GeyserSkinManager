package com.github.camotoy.geyserskinmanager.common.skinretriever;

import com.github.camotoy.geyserskinmanager.common.RawSkin;

import java.util.UUID;

public interface BedrockSkinRetriever {
    RawSkin getBedrockSkin(String name);

    RawSkin getBedrockSkin(UUID uuid);

    boolean isBedrockPlayer(UUID uuid);
}
