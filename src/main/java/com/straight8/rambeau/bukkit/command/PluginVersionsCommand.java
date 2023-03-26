package com.straight8.rambeau.bukkit.command;

import com.straight8.rambeau.bukkit.PluginVersionsBukkit;

import dev.ratas.slimedogcore.impl.commands.BukkitFacingParentCommand;
import com.straight8.rambeau.bukkit.command.sub.ListSub;
import com.straight8.rambeau.bukkit.command.sub.ReloadSub;

public class PluginVersionsCommand extends BukkitFacingParentCommand {

    public PluginVersionsCommand(PluginVersionsBukkit plugin) {
        addSubCommand(new ListSub(plugin));
        addSubCommand(new ReloadSub(plugin));
    }

}