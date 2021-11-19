package com.github.camotoy.geyserskinmanager.common;

import java.util.function.Consumer;

public final class FloodgateUtil {
    /**
     * @return true if Floodgate is present on the server.
     */
    public static boolean isFloodgatePresent(Consumer<String> warnFunction) {
        boolean floodgatePresent = false;
        if (!(Configuration.forceSkin)) {
            try {
                // Should Floodgate be present, don't bother
                Class.forName("org.geysermc.floodgate.api.FloodgateApi");
                floodgatePresent = System.getProperty("GeyserSkinManager.ForceShowSkins") == null;
                if (floodgatePresent) {
                    warnFunction.accept("Floodgate found on the server! Disabling skin services and only running mod interactions.");
                } else {
                    warnFunction.accept("Showing skins despite Floodgate being installed!");
                }
            } catch (Exception ignored) {
            }
        }

        return floodgatePresent;
    }
}
