package com.github.camotoy.geyserskinmanager.velocity;

import com.github.camotoy.geyserskinmanager.common.Constants;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

public class VelocityConstants {
    public static final ChannelIdentifier SKIN_PLUGIN_MESSAGE_NAME = MinecraftChannelIdentifier.from(Constants.SKIN_PLUGIN_MESSAGE_NAME);
    public static final ChannelIdentifier MOD_PLUGIN_MESSAGE_NAME = MinecraftChannelIdentifier.from(Constants.MOD_PLUGIN_MESSAGE_NAME);
}
