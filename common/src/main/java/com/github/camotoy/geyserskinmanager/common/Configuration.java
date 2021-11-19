package com.github.camotoy.geyserskinmanager.common;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Configuration {

    public  CommentedConfigurationNode root;
    public static boolean forceSkin;

    public void loadConfigFile(File dataFolder) {

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(Path.of(dataFolder + "config.yml"))
                .build();
        try {
            root = loader.load();
        } catch (IOException e) {
            System.err.println("An error occurred while loading this configuration: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            System.exit(1);
        }
        forceSkin = root.node("ForceShowSkins").getBoolean();
    }
}
