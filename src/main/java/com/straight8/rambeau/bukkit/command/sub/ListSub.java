package com.straight8.rambeau.bukkit.command.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.plugin.Plugin;

import com.straight8.rambeau.bukkit.PluginComparator;
import com.straight8.rambeau.bukkit.PluginVersionsBukkit;

import dev.ratas.slimedogcore.api.commands.SDCCommandOptionSet;
import dev.ratas.slimedogcore.api.messaging.factory.SDCDoubleContextMessageFactory;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCPlayerRecipient;
import dev.ratas.slimedogcore.api.messaging.recipient.SDCRecipient;
import dev.ratas.slimedogcore.impl.commands.AbstractSubCommand;

public class ListSub extends AbstractSubCommand {
    private static final String NAME = "list";
    private static final String PERMS = "pluginversions.list";
    private static final String USAGE = "/pv list [page]";
    private static final int LINES_PER_PAGE = 10;
    private final PluginVersionsBukkit plugin;

    public ListSub(PluginVersionsBukkit plugin) {
        super(NAME, PERMS, USAGE, true, false);
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(SDCRecipient sender, String[] args) {
        if (args.length == 1 && !(sender instanceof SDCPlayerRecipient)) {
            return getNextInteger(args[0],
                    (plugin.getServer().getPluginManager().getPlugins().length + LINES_PER_PAGE - 1) / LINES_PER_PAGE);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean onOptionedCommand(SDCRecipient sender, String[] args, SDCCommandOptionSet opts) {
        Plugin[] pluginList = plugin.getServer().getPluginManager().getPlugins();
        if (pluginList.length == 0) {
            sender.sendRawMessage("No plugins loaded");
            return true;
        }
        Arrays.sort(pluginList, new PluginComparator());

        // Identify the page to display. Page 0 indicates the entire list.
        int page = 0;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (Exception ignored) {
            }
        }
        // Set page to 0 if illegal page was requested
        page = Math.max(page, 0);

        if (page > 0) {
            if (((page - 1) * LINES_PER_PAGE) < pluginList.length) {
                sender.sendMessage(plugin.getMessages().getPageHeader().createWith(page));
            }
            for (int i = ((page - 1) * LINES_PER_PAGE); i < pluginList.length && i < (page * LINES_PER_PAGE); i++) {
                Plugin p = pluginList[i];

                SDCDoubleContextMessageFactory<String, String> msg;
                if (p.isEnabled()) {
                    msg = plugin.getMessages().getEnabledVersion();
                } else {
                    msg = plugin.getMessages().getDisabledVersion();
                }
                sender.sendMessage(msg.createWith(p.getName(), p.getDescription().getVersion()));
            }
        } else {
            for (Plugin p : pluginList) {
                SDCDoubleContextMessageFactory<String, String> msg;
                if (p.isEnabled()) {
                    msg = plugin.getMessages().getEnabledVersion();
                } else {
                    msg = plugin.getMessages().getDisabledVersion();
                }
                sender.sendMessage(msg.createWith(p.getName(), p.getDescription().getVersion()));
            }
        }
        return true;
    }

    private static final boolean isInteger(String str) {
        try {
            Integer.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static final List<String> getNextInteger(String argument, int maxPage) {
        List<String> options = new ArrayList<>();
        int curNr;
        if (argument.isEmpty()) {
            curNr = 0;
        } else if (!isInteger(argument)) {
            return options;
        } else {
            curNr = Integer.valueOf(argument);
        }
        if (curNr * 10 > maxPage) {
            return options;
        }
        int example = 10 * curNr;
        while (example <= maxPage && example < 10 * (curNr + 1)) {
            if (example != 0) {
                // don't send 0
                options.add(String.valueOf(example));
            }
            example++;
        }
        return options;
    }

}
