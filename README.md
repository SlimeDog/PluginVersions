**Introduction**  
Like most server operators, we use a large number of Spigot/Bukkit plugins. Keeping track of available upgrades can be frustrating. We rename jars to indicate versions (which requires that we type accurately). We watch Spigot resource forums to get notification of updates (but not useful for Bukkit-listed plugins). We wrote bash/screen scripts to supplement _/version_ (once for each plugin returned by _/plugins_) which were broken by the spigot.org CloudFlare D-DOS protection. We decided to write a plugin to address our needs. Perhaps others will find it useful.

**Product Description**  
Create an alphabetically-sorted list of loaded plugins and versions.

**Tested Environments**  
The plugin has been tested on the following Spigot versions. It should work on other Minecraft modifications and versions, but we have not tested them and do not intend to do so. We will  address promptly any issues reported.

> Spigot 1.9  
> Spigot 1.8.8  

**Commands 1.0**  
All commands may be executed in-game or at the console.  

> _pv list_  
> Create an alphabetically-sorted columnar list of plugins and versions.
> 
> _pv reload_  
> Reload configuration file.
> At present, the only configuration option is whether to send metrics to MCStats.org.
  
**Permissions**  
Permissions reflect the commands they allow. All permissions default to op.

> _pluginversions.list_  
> _pluginversions.reload_  


