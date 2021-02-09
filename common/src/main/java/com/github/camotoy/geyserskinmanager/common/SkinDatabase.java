package com.github.camotoy.geyserskinmanager.common;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SkinDatabase {
    private static final DefaultPrettyPrinter PRETTY_PRINTER = new DefaultPrettyPrinter();

    private final Map<UUID, byte[]> capeEntries = new ConcurrentHashMap<>();
    private final Cache<UUID, PlayerEntry> playerEntries = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build();
    private final File baseFileLocation;

    public SkinDatabase(File baseFileLocation) {
        this.baseFileLocation = baseFileLocation.toPath().resolve("users").toFile();
        if (!this.baseFileLocation.exists()) {
            //noinspection ResultOfMethodCallIgnored
            this.baseFileLocation.mkdir();
        }
    }

    public void savePlayerInformation(PlayerEntry entry) {
        String playerUuid = entry.getPlayerUuid().toString();
        File playerFileLocation = baseFileLocation.toPath().resolve(playerUuid + ".json").toFile();

        ObjectNode node = SkinUtils.OBJECT_MAPPER.createObjectNode();
        node.put("playerUuid", playerUuid);
        ArrayNode jsonSkinEntries =  SkinUtils.OBJECT_MAPPER.createArrayNode();

        while (entry.getSkinEntries().size() > 5) { // Make this number a config option in the future?
            // Remove old skin entries
            long oldestDate = Long.MAX_VALUE;
            SkinEntry oldestSkinEntry = null;
            for (SkinEntry skinEntry : entry.getSkinEntries()) {
                if (oldestDate > skinEntry.getDateAdded()) {
                    oldestDate = skinEntry.getDateAdded();
                    oldestSkinEntry = skinEntry;
                }
            }
            entry.getSkinEntries().remove(oldestSkinEntry);
        }

        for (SkinEntry skinEntry : entry.getSkinEntries()) {
            ObjectNode jsonSkinEntry =  SkinUtils.OBJECT_MAPPER.createObjectNode();
            jsonSkinEntry.put("bedrockSkin", skinEntry.getBedrockBase64Skin());
            jsonSkinEntry.put("javaSkinValue", skinEntry.getJavaSkinValue());
            jsonSkinEntry.put("javaSkinSignature", skinEntry.getJavaSkinSignature());
            jsonSkinEntry.put("dateAdded", skinEntry.getDateAdded());
            jsonSkinEntries.add(jsonSkinEntry);
        }
        node.set("skinEntries", jsonSkinEntries);

        try (OutputStream outputStream = Files.newOutputStream(playerFileLocation.toPath(),
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            SkinUtils.OBJECT_MAPPER.writer(PRETTY_PRINTER).writeValue(outputStream, node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerEntry getPlayerEntry(UUID uuid) {
        PlayerEntry entry = playerEntries.getIfPresent(uuid);
        if (entry == null) {
            return loadPlayerInformation(uuid);
        } else {
            return entry;
        }
    }

    private PlayerEntry loadPlayerInformation(UUID uuid) {
        File playerFileLocation = baseFileLocation.toPath().resolve(uuid.toString() + ".json").toFile();
        if (!playerFileLocation.exists()) {
            return null;
        }
        try {
            JsonNode node =  SkinUtils.OBJECT_MAPPER.readTree(playerFileLocation);
            JsonNode jsonSkinEntries = node.get("skinEntries");
            List<SkinEntry> skinEntries = new ArrayList<>();
            for (JsonNode entry : jsonSkinEntries) {
                long dateAdded;
                JsonNode jsonDateAdded = entry.get("dateAdded");
                if (jsonDateAdded != null) {
                    dateAdded = jsonDateAdded.asLong();
                } else {
                    continue; // Pre-1.3-SNAPSHOT - Don't load these in as their geometry might not have been saved
                }

                skinEntries.add(new SkinEntry(entry.get("bedrockSkin").asText(), entry.get("javaSkinValue").asText(),
                        entry.get("javaSkinSignature").asText(), true, dateAdded));
            }
            PlayerEntry playerEntry = new PlayerEntry(UUID.fromString(node.get("playerUuid").asText()), skinEntries);
            playerEntries.put(uuid, playerEntry);
            return playerEntry;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addCape(UUID uuid, byte[] payload) {
        capeEntries.put(uuid, payload);
    }

    public Set<Map.Entry<UUID, byte[]>> getCapes() {
        return capeEntries.entrySet();
    }

    @Nullable
    public byte[] getCape(UUID uuid) {
        return capeEntries.get(uuid);
    }

    public void removeCape(UUID uuid) {
        capeEntries.remove(uuid);
    }

    public void clear() {
        capeEntries.clear();
        playerEntries.invalidateAll();
    }

}
