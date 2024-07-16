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

    /**
     * The Steve skin applied to Floodgate players with no skin.
     */
    public static final String FLOODGATE_STEVE_SKIN = "ewogICJ0aW1lc3RhbXAiIDogMTcxNTcxNzM1NTI2MywKICAicHJvZmlsZUlkIiA6ICIyMWUzNjdkNzI1Y2Y0ZTNiYjI2OTJjNGEzMDBhNGRlYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJHZXlzZXJNQyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zMWY0NzdlYjFhN2JlZWU2MzFjMmNhNjRkMDZmOGY2OGZhOTNhMzM4NmQwNDQ1MmFiMjdmNDNhY2RmMWI2MGNiIgogICAgfQogIH0KfQ";
}
