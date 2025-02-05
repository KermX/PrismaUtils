package me.kermx.prismaUtils.Commands.admin;

import me.kermx.prismaUtils.Commands.BaseCommand;
import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadConfigCommand extends BaseCommand {

    private final PrismaUtils plugin;

    public ReloadConfigCommand(PrismaUtils plugin){
        super("prismautils.command.reload", true, "/prismautilsreload");
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length > 0){
            return false;
        }
        plugin.reloadConfig();
        sender.sendMessage("PrismaUtils config reloaded!");
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        return super.onTabCompleteExecute(sender, args);
    }
}
