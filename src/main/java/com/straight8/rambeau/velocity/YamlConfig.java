package com.straight8.rambeau.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class YamlConfig {

    private File defaultCfg;
    private final Logger logger;
    private Path path;
    private final ProxyServer server;

    private final Path defaultConfig;
    private final File defaultConf;
    private CommentedConfigurationNode configNode;
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    public YamlConfig(Path defaultConfig, String configName, Logger logger, ProxyServer server) {
        this.defaultConfig = defaultConfig;
        this.logger = logger;
        this.server = server;

        if (!defaultConfig.toFile().exists()) {
            defaultConfig.toFile().mkdir();
        }

        defaultConf = new File(defaultConfig.toFile(), configName);
        configManager = HoconConfigurationLoader.builder().file(defaultConf).build();

        try {
            configNode = configManager.load();
            logger.info("Loading Config!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            saveConfig();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        save(defaultConf);
    }

    public CommentedConfigurationNode getConfigNode() {
        return configNode;
    }

    public void saveConfig() {
        try {
            configManager.save(configNode);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void save(File input) {
        configManager = HoconConfigurationLoader.builder().file(input).build();
    }
}