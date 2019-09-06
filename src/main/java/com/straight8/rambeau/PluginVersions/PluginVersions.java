// PluginVersions
// List versions of all loaded plugins, sorted alphabetically.
//		This is a combinaion of /plugins and /version plugin for each member of the list.
// Reload the config.yml file. If config.yml does not exist, copy it from the jar.
// FUTURE? Report available updates for loaded plugins.
// FUTURE? Update specific plugins or all loaded plugins.

package com.straight8.rambeau.PluginVersions;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

// import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
// Imports for Metrics
import org.bstats.bukkit.Metrics;


// FOR TESTING, BUT DELETE UNUSED CODE: @SuppressWarnings("unused")

public class PluginVersions extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	
    // Configuration values:
	private Boolean configurationSendMetrics = true;

	// Fired when plugin is first enabled
    @Override
    public void onEnable() {
    	// Enable is logged automatically.
    	
    	// Create config.yml and plugin directory tree, if they do not exist.
    	CreateConfigFileIfMissing();
    	
    	// Read the configuration values from config.yml.
    	ReadConfigValuesFromFile(false);
		
		// Submit plugin usage data to MCStats.org.
    	if (configurationSendMetrics == true) {
			new Metrics(this);
		}

		new UpdateChecker(this, (response,version)-> {
			switch(response){
			case LATEST:
				getLogger().info("Running latest version!");
				break;
			case UNAVAILABLE:
				getLogger().info("Unable to check for new version");
				break;
			case FOUND_NEW:
				getLogger().warning("Running outdated version! New version available:" + version);
				break;
			}
		}).check();
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
				} catch (Exception e) {
					page = 0;				
				}
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
			ReadConfigValuesFromFile(true);
			sender.sendMessage("Reloaded " + ChatColor.AQUA + this.getName() + "/config.yml" );
			return true;
			// break;

// TODO: FUTURES? IF POSSIBLE...
//			case "check-update":
//			private Boolean updateAvailable = false;
//			sender.sendMessage("Run " + cmd + " " + commandLowerCase);
//			return true;
//			// break;
			
//		case "update":
//			sender.sendMessage("Run " + cmd + " " + commandLowerCase);
//			return true;
//			// break;
			
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
    				return;
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

    public void ReadConfigValuesFromFile(Boolean reportChanges) {
    	int countChangesOnReload = 0;
    	// Get inMemory value.
    	FileConfiguration inMemoryConfig = getConfig();
       	String inMemoryConfigString = inMemoryConfig.getString("metrics");
       	// TESTING: this.log("inMemoryConfigString=[" + inMemoryConfigString + "]");
       	
       	// Get (possibly) new value from file.
       	this.reloadConfig();
       	FileConfiguration reloadedConfig = getConfig();

       	// Evaluate metrics: true | false
       	String reloadedConfigString = reloadedConfig.getString("metrics");
		if (reloadedConfigString == null) {
    		if (inMemoryConfigString.contains("true")) {
        		configurationSendMetrics = true;
     		}
    		else {
        		configurationSendMetrics = false;
    		}
    		return;
    	}		
   		reloadedConfigString = reloadedConfigString.replace("'", "") + ",";
    	if (reloadedConfigString.contains("true")) {
    		configurationSendMetrics = true;
    		if (inMemoryConfigString.contains("false")) {
	   			++countChangesOnReload;
	   			this.log("config value changed: metrics=true; metrics will be sent");
    		}
    	} else {
       		if (reloadedConfigString.contains("false")) {
        		configurationSendMetrics = false;
       			if (inMemoryConfigString.contains("true")) {
    	   			++countChangesOnReload;
    	   			this.log("config value changed: metrics=false; metrics will not be sent");
       			}
       		}
    	}
		if (countChangesOnReload == 0) {
			this.log("no config values were changed on reload");
		}
    	return;
    }

    public void log(String logString) {
    	this.logger.info("[" + this.getName() + "] " + logString);
    }
    
}
