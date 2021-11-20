package com.straight8.rambeau.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginVersionsCmd extends Command {

    private final PluginVersionsBungee plugin;

    public PluginVersionsCmd(PluginVersionsBungee plugin) {
        super("pluginversions", "pluginversions.list", "pvb");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return;
        }
        String cmdLowercase = args[0].toLowerCase();

        if (sender instanceof ProxiedPlayer) {
            if (!sender.hasPermission("pluginversions." + cmdLowercase)) {
                String senderName = sender.getName();
                sender.sendMessage("You do not have permission to run this command");
                this.plugin.log(senderName + " attempted to run command pv " + cmdLowercase + ", but lacked permissions");
                return;
            }
        }

        if (cmdLowercase.equals("list")) {
            List<Plugin> pluginList = new ArrayList<>(this.plugin.getProxy().getPluginManager().getPlugins());

            if (pluginList.isEmpty()) {
                sender.sendMessage("No plugins loaded");
                return;
            }

            pluginList.sort(new PluginComparator());

            // Identify the page to display. Page 0 indicates the entire list.
            int page = 0;
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (Exception ignored) {
                }
            }
            // Set page to 0 if illegal page was requested
            page = Math.max(page, 0);

            String formatString;
            if (sender instanceof ProxiedPlayer) {
                // Output only one page if the command was invoked in-game
                if (page == 0) {
                    page = 1;
                }
                // In-game chat font is variable-pitch, so tabular format is pointless.
                formatString = String.format("%%s %%s");
            } else {
                // Construct a tablular format for fixed-pitch fonts, like log and console.
                int n = 1;
                for (Plugin p : pluginList) {
                    n = Math.max(n, p.getDescription().getName().length());
                }
                formatString = String.format("%%-%ds %%s", n);
            }

            int linesPerPage = 10;
            if (page > 0) {
                if (((page - 1) * linesPerPage) < pluginList.size()) {
                    sender.sendMessage("PluginVersions ===== page " + page + " =====");
                }
                for (int i = ((page - 1) * linesPerPage); i < pluginList.size() && i < (page * linesPerPage); i++) {
                    Plugin p = pluginList.get(i);
                    sender.sendMessage(String.format(formatString,
                            p.getDescription().getName(), p.getDescription().getVersion()));
                }
            } else {
                for (Plugin p : pluginList) {
                    sender.sendMessage(String.format(formatString,
                            p.getDescription().getName(), p.getDescription().getVersion()));
                }
            }
            // break;
        } else if(cmdLowercase.equals("reload")) {
            YamlConfig.createFiles("config");
            PluginVersionsBungee.getInstance().ReadConfigValuesFromFile();

            sender.sendMessage("Reloaded §bPluginVersions/config.yml" );
        } else {
            sender.sendMessage("Unrecognized command option " + cmdLowercase);
        }
    }
}
