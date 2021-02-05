package com.github.camotoy.geyserskinmanager.common.platform;

import java.util.UUID;

/**
 * @param <T> the player class of this platform - ProxiedPlayer for BungeeCord, Player for Spigot, and so on.
 */
public interface PlatformPlayerUuidSupport<T> {
    UUID getUUID(T player);
}
