package com.straight8.rambeau.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

public class PluginVersionsCmd implements SimpleCommand {

    private final PluginVersionsVelocity plugin;

    public PluginVersionsCmd(PluginVersionsVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        // Get the arguments after the command alias
        String[] args = invocation.arguments();

        if (args.length == 0) {
            return;
        }
        String cmdLowercase = args[0].toLowerCase();

        if (sender instanceof Player) {
            if (!sender.hasPermission("pluginversions." + cmdLowercase)) {
                String senderName = ((Player) sender).getUsername();
                sender.sendMessage(Component.text("You do not have permission to run this command"));
                this.plugin.log(senderName + " attempted to run command pv " + cmdLowercase + ", but lacked permissions");
                return;
            }
        }

        if (cmdLowercase.equals("list")) {
            List<PluginContainer> pluginList = new ArrayList<>(this.plugin.getServer().getPluginManager().getPlugins());

            if (pluginList.isEmpty()) {
                sender.sendMessage(Component.text("No plugins loaded"));
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
                for (PluginContainer p : pluginList) {
                    n = Math.max(n, p.getDescription().getName().get().length());
                }
                formatString = String.format("%%-%ds %%s", n);
            }

            int linesPerPage = 10;
            if (page > 0) {
                if (((page - 1) * linesPerPage) < pluginList.size()) {
                    sender.sendMessage(Component.text("PluginVersions ===== page " + page + " ====="));
                }
                for (int i = ((page - 1) * linesPerPage); i < pluginList.size() && i < (page * linesPerPage); i++) {
                    PluginContainer p = pluginList.get(i);
                    sender.sendMessage(Component.text(String.format(formatString,
                            p.getDescription().getName().get(), p.getDescription().getVersion().get())));
                }
            } else {
                for (PluginContainer p : pluginList) {
                    sender.sendMessage(Component.text(String.format(formatString,
                            p.getDescription().getName().get(), p.getDescription().getVersion().get())));
                }
            }
            // break;
        } else if(cmdLowercase.equals("reload")) {
            YamlConfig.createFiles("config");
            PluginVersionsVelocity.getInstance().ReadConfigValuesFromFile();

            sender.sendMessage(Component.text("Reloaded §bPluginVersions/config.yml"));
        } else {
            sender.sendMessage(Component.text("Unrecognized command option " + cmdLowercase));
        }
    }
}
