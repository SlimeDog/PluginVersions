package com.straight8.rambeau.velocity;

import com.google.inject.Inject;
import com.straight8.rambeau.metrics.VelocityMetrics;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.nio.file.Path;

@Plugin(id = "pluginversions", name = "PluginVersions", version = "1.0.6", description = "List installed plugins and versions alphabetically", authors = {"SlimeDog", "drives_a_ford"})
public class PluginVersionsVelocity {

    private static PluginVersionsVelocity instance;
    private YamlConfig yamlConfig;

    private final Path dataDirectory;
    private final ProxyServer server;
    private final Logger logger;

    private boolean configurationSendMetrics = true;
    private boolean checkUpdates = true;
    private final VelocityMetrics.Factory metricsFactory;

    @Inject
    public PluginVersionsVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, VelocityMetrics.Factory metricsFactory) {
        instance = this;

        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.yamlConfig = new YamlConfig(this.dataDirectory, "config.yml", logger, server);

        this.ReadConfigValuesFromFile();

        if (configurationSendMetrics) {

            // All you have to do is adding the following two lines in your onProxyInitialization method.
            // You can find the plugin ids of your plugins on the page https://bstats.org/what-is-my-plugin-id
            metricsFactory.make(this, 13032);
        }

        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("pluginversions")
                // Specify other aliases (optional)
                .aliases("pv")
                .build();

        commandManager.register(meta, new PluginVersionsCmd(this));
    }

    public static PluginVersionsVelocity getInstance() {
        return instance;
    }

    public void ReadConfigValuesFromFile() {
        CommentedConfigurationNode configNode = this.yamlConfig.getConfigNode();

        // Optimized the code to read the configuration options
        configurationSendMetrics = configNode.node("enable-metrics").getBoolean(true);
        checkUpdates = configNode.node("check-for-updates").getBoolean(true);
    }

    public void log(String logString) {
        this.logger.info("[PluginVersions] " + logString);
    }

    public ProxyServer getServer() {
        return server;
    }
}
