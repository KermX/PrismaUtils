package me.kermx.prismaUtils.managers;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

public class CommandManager {
    private final PrismaUtils plugin;

    public CommandManager(PrismaUtils plugin){
        this.plugin = plugin;
    }

    public void registerCommand(String commandName, CommandExecutor executor, TabCompleter tabCompleter){
        if (plugin.getCommand(commandName) != null){
            plugin.getCommand(commandName).setExecutor(executor);
            plugin.getCommand(commandName).setTabCompleter(tabCompleter);
        } else {
            plugin.getLogger().warning("Command " + commandName + " is not defined in plugin.yml!");
        }
    }
}
