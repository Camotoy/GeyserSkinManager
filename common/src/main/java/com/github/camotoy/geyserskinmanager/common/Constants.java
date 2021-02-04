package com.github.camotoy.geyserskinmanager.common;

/**
 * A collection of static variables used across versions.
 */
public final class Constants {
    /**
     * The version sent and checked as the first component of an incoming plugin message.
     * Used for future-proofing, in case the contents of a plugin message changes.
     */
    public static final int PLUGIN_MESSAGE_VERSION = 1;
    public static final String SKIN_PLUGIN_MESSAGE_NAME = "geyserskinmanager:skin";
}
