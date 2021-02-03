package com.github.camotoy.geyserskinmanager.common.skinretriever;

import com.github.camotoy.geyserskinmanager.common.RawSkin;

import java.util.UUID;

public interface BedrockSkinRetriever {
    RawSkin getBedrockSkin(UUID uuid);
}
