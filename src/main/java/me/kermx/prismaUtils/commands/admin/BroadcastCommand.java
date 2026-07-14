package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BroadcastCommand extends BaseCommand {

    public BroadcastCommand() {
        super("prismautils.command.broadcast", true, "/broadcast <message>");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }

        String message = String.join(" ", args);
        Bukkit.broadcast(TextUtils.deserializeFlexible(message));
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}
