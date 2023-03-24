package com.straight8.rambeau.velocity;

import com.straight8.rambeau.util.CommandPageUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PluginVersionsCmd implements SimpleCommand {
    private static final int LINES_PER_PAGE = 10;

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

            if (page > 0) {
                if (((page - 1) * LINES_PER_PAGE) < pluginList.size()) {
                    String msg = plugin.getConfig().getString("page-header-format",
                            "PluginVersions ===== page {page} =====");
                    sender.sendMessage(Component.text(msg.replace("{page}", String.valueOf(page))));
                }
                int maxSpacing = CommandPageUtils.getMaxNameLength(plugin -> {
                    Optional<String> name = plugin.getDescription().getName();
                    return name.isPresent() ? name.get() : "N/A";
                }, CommandPageUtils.getPage(pluginList, page, LINES_PER_PAGE));
                for (int i = ((page - 1) * LINES_PER_PAGE); i < pluginList.size() && i < (page * LINES_PER_PAGE); i++) {
                    PluginContainer p = pluginList.get(i);

                    String msg = plugin.getConfig().getString("enabled-version-format", "&a{name}{spacing}&e{version}");
                    if(p.getDescription().getName().isPresent() && p.getDescription().getVersion().isPresent()) {
                        String spacing = CommandPageUtils.getSpacingFor(p.getDescription().getName().get(), maxSpacing);
                        Component comp = Component
                                .text(msg.replace("{name}", p.getDescription().getName().get()).replace("{version}",
                                        p.getDescription().getVersion().get()).replace("{spacing}", spacing));
                        sender.sendMessage(comp);
                    } else if(p.getDescription().getId().equalsIgnoreCase("serverlistplus")) {
                        String spacing = CommandPageUtils.getSpacingFor(p.getDescription().getName().get(), maxSpacing);
                        Component comp = Component
                                .text(msg.replace("{name}", SLPUtils.getSLPName()).replace("{version}",
                                        SLPUtils.getSLPVersion()).replace("{spacing}", spacing));
                        sender.sendMessage(comp);
                    }
                }
            } else {
                int maxSpacing = CommandPageUtils.getMaxNameLength(plugin -> {
                    Optional<String> name = plugin.getDescription().getName();
                    return name.isPresent() ? name.get() : "N/A";
                }, pluginList);
                for (PluginContainer p : pluginList) {
                    String msg = plugin.getConfig().getString("enabled-version-format", "&a{name}{spacing}&e{version}");
                    if(p.getDescription().getName().isPresent() && p.getDescription().getVersion().isPresent()) {
                        String spacing = CommandPageUtils.getSpacingFor(p.getDescription().getName().get(), maxSpacing);
                        Component comp = Component
                                .text(msg.replace("{name}", p.getDescription().getName().get()).replace("{version}",
                                        p.getDescription().getVersion().get()).replace("{spacing}", spacing));
                        sender.sendMessage(comp);
                    } else if(p.getDescription().getId().equalsIgnoreCase("serverlistplus")) {
                        String spacing = CommandPageUtils.getSpacingFor(p.getDescription().getName().get(), maxSpacing);
                        Component comp = Component
                                .text(msg.replace("{name}", p.getDescription().getName().get()).replace("{version}",
                                        p.getDescription().getVersion().get()).replace("{spacing}", spacing));
                        sender.sendMessage(comp);
                    }
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
