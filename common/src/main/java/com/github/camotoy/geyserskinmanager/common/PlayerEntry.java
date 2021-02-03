package com.github.camotoy.geyserskinmanager.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerEntry {
    private final UUID playerUuid;
    private final List<SkinEntry> skinEntries;

    public PlayerEntry(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.skinEntries = new ArrayList<>();
    }

    public PlayerEntry(UUID playerUuid, List<SkinEntry> skinEntries) {
        this.playerUuid = playerUuid;
        this.skinEntries = skinEntries;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public List<SkinEntry> getSkinEntries() {
        return skinEntries;
    }

    @Override
    public String toString() {
        return "PlayerEntry{" +
                "playerUuid=" + playerUuid +
                ", skinEntries=" + skinEntries +
                '}';
    }
}
