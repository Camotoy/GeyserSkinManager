package com.github.camotoy.geyserskinmanager.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigurationJackson {

    @JsonProperty("force-show-skins")
    private boolean forceShowSkins;

    public void SetForceSkin(Boolean forceSkin) {
        this.forceShowSkins = forceSkin;
    }

    public Boolean getForceShowSkins() {
        return forceShowSkins;
    }
}
