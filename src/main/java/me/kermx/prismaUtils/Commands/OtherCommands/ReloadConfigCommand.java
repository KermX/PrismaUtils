package me.kermx.prismaUtils.Commands.OtherCommands;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadConfigCommand implements CommandExecutor {

    private final PrismaUtils plugin;

    public ReloadConfigCommand(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("prismautils.command.reload")) {
            sender.sendMessage("You don't have permission to perform this command!");
            return false;
        }

        plugin.reloadConfig();
        sender.sendMessage("PrismaUtils config reloaded!");
        return true;
    }
}
