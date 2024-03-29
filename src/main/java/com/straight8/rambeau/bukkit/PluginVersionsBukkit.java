// PluginVersions
// List versions of all loaded plugins, sorted alphabetically.
//		This is a combinaion of /plugins and /version plugin for each member of the list.
// Reload the config.yml file. If config.yml does not exist, copy it from the jar.
// FUTURE? Report available updates for loaded plugins.
// FUTURE? Update specific plugins or all loaded plugins.

package com.straight8.rambeau.bukkit;

import com.straight8.rambeau.bukkit.command.PluginVersionsCommand;
import com.straight8.rambeau.metrics.SpigotMetrics;

import dev.ratas.slimedogcore.impl.SlimeDogCore;
import dev.ratas.slimedogcore.impl.utils.UpdateChecker;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

// import org.bukkit.Bukkit;
// Imports for Metrics

public class PluginVersionsBukkit extends SlimeDogCore {
	private static final int SPIGOT_ID = 5509;
	private static final String HANGAR_AUTHOR = "SlimeDog";
	private static final String HANGAR_SLUG = "PluginVersions";
	public final Logger logger = Logger.getLogger("Minecraft");

	private Messages messages;
	
    // Configuration values:
	private boolean configurationSendMetrics = true;
	private boolean checkUpdates = true;

	// Fired when plugin is first enabled
    @Override
    public void pluginEnabled() {
    	// Enable is logged automatically.
    	
    	// Create config.yml and plugin directory tree, if they do not exist.
    	CreateConfigFileIfMissing();
    	
    	// Read the configuration values from config.yml.
    	ReadConfigValuesFromFile();
		
		// Submit plugin usage data to MCStats.org.
    	if (configurationSendMetrics) {
			new SpigotMetrics(this, 5509);
		}

		if (checkUpdates) {
            String source = getDefaultConfig().getConfig().getString("update-source", "Hangar");
            BiConsumer<UpdateChecker.VersionResponse, String> consumer = (response, version) -> {
                switch (response) {
                    case LATEST:
                        getLogger().info("Already on latest version");
                        break;
                    case FOUND_NEW:
                        getLogger().info("Found new version: " + version);
                        break;
                    case UNAVAILABLE:
                        getLogger().info("Version information not available");
                        break;
                }
            };
            UpdateChecker checker;
            if (source.equalsIgnoreCase("Hangar")) {
                checker = UpdateChecker.forHangar(this, consumer, HANGAR_AUTHOR, HANGAR_SLUG);
            } else {
                checker = UpdateChecker.forSpigot(this, consumer, SPIGOT_ID);
            }
            checker.check();
		}

		messages = new Messages(getDefaultConfig());

		getCommand("pluginversions").setExecutor(new PluginVersionsCommand(this));
    }

	public Messages getMessages() {
		return messages;
	}
    
    // Fired when plugin is disabled
    @Override
    public void pluginDisabled() {
    	// Disable is logged automatically.
	}
	
    public void CreateConfigFileIfMissing() {
    	try {
    		PluginDescriptionFile pdfFile = this.getDescription();
    		
    		if (!getDataFolder().exists()) {
    			this.log(pdfFile.getName() + ": folder doesn't exist");
    			this.log(pdfFile.getName() + ": creating folder");
    			try {
    				getDataFolder().mkdirs();
    			} catch(Exception e) {
        			this.log(pdfFile.getName() + ": could not create folder");
    				return;
    			}
    			this.log(pdfFile.getName() + ": folder created at " + getDataFolder());
    		}
    		
    		File configFile = new File(getDataFolder(), "config.yml");
    		if (!configFile.exists()) {
    			this.log(pdfFile.getName() + ": config.yml not found, creating!");
    			// Copy config.yml from the jar.
    			try {
    				saveDefaultConfig();
    			} catch(Exception e) {
        			this.log(pdfFile.getName() + ": could not save config.yml");
				}
    			// Do not saveConfig() or comments below the header will be deleted.
    			// There are  code samples on the internet that resolve the issue,
    			// but not needed, since we don't want to change values on the fly.
    			// FileConfiguration config = getConfig();
    			// config.options().copyDefaults(true);
    			// saveConfig();
    		}
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }

    public void ReadConfigValuesFromFile() {
		this.reloadConfig();

		FileConfiguration reloadedConfig = getConfig();
		// Optimized the code to read the configuration options
		configurationSendMetrics = reloadedConfig.getBoolean("enable-metrics", true);
		checkUpdates = reloadedConfig.getBoolean("check-for-updates", true);
		if (messages != null) { // ignore first time around
			messages.reload();
		}
	}

    public void log(String logString) {
    	this.logger.info("[" + this.getName() + "] " + logString);
    }
}
