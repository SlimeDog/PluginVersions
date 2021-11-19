// PluginVersions
// List versions of all loaded plugins, sorted alphabetically.
//		This is a combinaion of /plugins and /version plugin for each member of the list.
// Reload the config.yml file. If config.yml does not exist, copy it from the jar.
// FUTURE? Report available updates for loaded plugins.
// FUTURE? Update specific plugins or all loaded plugins.

package com.straight8.rambeau.bukkit;

import com.straight8.rambeau.metrics.SpigotMetrics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

// import org.bukkit.Bukkit;
// Imports for Metrics

public class PluginVersionsBukkit extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	
    // Configuration values:
	private boolean configurationSendMetrics = true;
	private boolean checkUpdates = true;

	// Fired when plugin is first enabled
    @Override
    public void onEnable() {
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
			new UpdateChecker(this, (response, version)-> {
				switch (response) {
					case LATEST -> getLogger().info("Running latest version!");
					case UNAVAILABLE -> getLogger().info("Unable to check for new version");
					case FOUND_NEW -> getLogger().warning("Running outdated version! New version available:" + version);
				}
			}).check();
		}
    }
    
    // Fired when plugin is disabled
    @Override
    public void onDisable() {
    	// Disable is logged automatically.
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			return true;
		}
		String cmdLowercase = args[0].toLowerCase();

		if (sender instanceof Player) {
			if (!sender.hasPermission("pluginversions." + cmdLowercase)) {
				String senderName = getName();
				sender.sendMessage("You do not have permission to run this command");
				this.log(senderName + " attempted to run command pv " + cmdLowercase + ", but lacked permissions");
				return true;
			}
		}
		
		switch (cmdLowercase) {
		case "list":
			Plugin[] pluginList = getServer().getPluginManager().getPlugins();
			if (pluginList.length == 0) {
				sender.sendMessage("No plugins loaded");
				return true;
			}
			Arrays.sort(pluginList, new PluginComparator());
			
			// Identify the page to display. Page 0 indicates the entire list.
			int page = 0;
			if (args.length > 1) {
				try {
					page = Integer.parseInt(args[1]);
				} catch (Exception ignored) {}
			}
			// Set page to 0 if illegal page was requested
			page = Math.max(page, 0);

			String formatString;
			if (sender instanceof Player) {
				// Output only one page if the command was invoked in-game
				if (page == 0) {
					page = 1;
				}
				// In-game chat font is variable-pitch, so tabular format is pointless.
				formatString = String.format("%%s %%s");
			} else {
				// Construct a tablular format for fixed-pitch fonts, like log and console.
				int n = 1;
				for ( Plugin p : pluginList ) {
					n = Math.max(n, p.getName().length());
				}
				formatString = String.format("%%-%ds %%s", n);
			}

			int linesPerPage = 10;
			if (page > 0) {
				if (((page-1)*linesPerPage) < pluginList.length) {
					sender.sendMessage("PluginVersions ===== page " + page + " =====");
				}
				for ( int i=((page-1)*linesPerPage); i<pluginList.length && i<(page*linesPerPage); i++ ) {
					Plugin p = pluginList[i];
					sender.sendMessage(String.format(formatString,
							p.getName(), p.getDescription().getVersion()));
				}
			}
			else {
				for ( Plugin p : pluginList ) {
					sender.sendMessage(String.format(formatString,
						p.getName(), p.getDescription().getVersion()));
				}
			}
			return true;
			// break;
			
		case "reload":
			CreateConfigFileIfMissing();
			ReadConfigValuesFromFile();
			sender.sendMessage("Reloaded " + ChatColor.AQUA + this.getName() + "/config.yml" );
			return true;
			
		default:
			sender.sendMessage("Unrecognized command option " + cmdLowercase);
			return false;
			// break;
		}
	}
	
    private void CreateConfigFileIfMissing() {
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
	}

    public void log(String logString) {
    	this.logger.info("[" + this.getName() + "] " + logString);
    }
}
