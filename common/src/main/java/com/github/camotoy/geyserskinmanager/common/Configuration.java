package com.github.camotoy.geyserskinmanager.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Configuration {

    private Boolean ForceShowSkins;

    public Configuration(Path dataDirectory) throws IOException {
        createConfig(dataDirectory);
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Configuration config = mapper.readValue(new File(dataDirectory + "config.yml"), Configuration.class);
        if (config.getForceShowSkins()) {
            FloodgateUtil.setForceSkin(true);
        }
    }
    public void SetForceSkin(Boolean forceSkin) {
        this.ForceShowSkins = forceSkin;
    }

    public Boolean getForceShowSkins() {
        return ForceShowSkins;
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
