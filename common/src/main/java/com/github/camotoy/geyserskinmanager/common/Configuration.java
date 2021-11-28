package com.github.camotoy.geyserskinmanager.common;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Configuration {

    public Configuration(Path dataDirectory) {
        Toml config = loadConfig(dataDirectory);
        if (Objects.requireNonNull(config).getBoolean("ForceShowSkins")){
            FloodgateUtil.setForceSkin(true);
        }
    }

    /**
     * Load GeyserSkinManager config
     *
     * @param path The config's directory
     * @return The configuration
     */
    private Toml loadConfig(Path path) {
        File folder = path.toFile();
        File file = new File(folder, "config.toml");

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
                return null;
            }
        }
        return new Toml().read(file);
    }
}
