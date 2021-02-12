package com.github.camotoy.geyserskinmanager.common;

/**
 * A collection of static variables used across versions.
 */
public final class Constants {
    public static final String MOD_PLUGIN_MESSAGE_NAME = "bedrockskin:data";
    /**
     * The version sent and checked as the first component of a cape plugin message.
     * Used for future-proofing, in case the contents of a plugin message changes.
     */
    public static final int CAPE_PLUGIN_MESSAGE_TYPE_VERSION = 1;
    public static final int SKIN_INFO_PLUGIN_MESSAGE_TYPE_VERSION = 1;
    public static final int SKIN_DATA_PLUGIN_MESSAGE_TYPE_VERSION = 1;

    public static final String SKIN_PLUGIN_MESSAGE_NAME = "geyserskin:skin";
    /**
     * The version sent and checked as the first component of a plugin message.
     * Used for future-proofing, in case the contents of a plugin message changes.
     */
    public static final int SKIN_PLUGIN_MESSAGE_VERSION = 1;
}
