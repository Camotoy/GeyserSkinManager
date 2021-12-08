package com.github.camotoy.geyserskinmanager.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Configuration {

    public Configuration() {
        // Empty for Jackson usage
    }

    /**
     * Load GeyserSkinManager config
     *
     * @param dataDirectory The config's directory
     */
    public static Configuration create(Path dataDirectory) {
        File folder = dataDirectory.toFile();
        File file = new File(folder, "config.yml");

        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (InputStream input = Configuration.class.getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        // Read config
        try {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(dataDirectory.resolve("config.yml").toFile(), Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create GeyserSkinManager config!", e);
        }
    }

    @JsonProperty("force-show-skins")
    private boolean forceShowSkins;

    public boolean getForceShowSkins() {
        return forceShowSkins;
    }
}
