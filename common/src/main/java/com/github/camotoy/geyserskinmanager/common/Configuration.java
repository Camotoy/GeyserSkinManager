package com.github.camotoy.geyserskinmanager.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Configuration {

    public Configuration(Path dataDirectory) {
        createConfig(dataDirectory);
        // Read config and boolean.
        try {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ConfigurationJackson config = mapper.readValue(new File(dataDirectory + "\\" + "config.yml"), ConfigurationJackson.class);
        if (config.getForceShowSkins()) {
            FloodgateUtil.setForceSkin(true);
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load GeyserSkinManager config
     *
     * @param path The config's directory
     */
    private void createConfig(Path path) {
        File folder = path.toFile();
        File file = new File(folder, "config.yml");

        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (InputStream input = getClass().getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
