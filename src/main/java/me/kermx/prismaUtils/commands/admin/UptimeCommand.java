package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.command.CommandSender;

import java.lang.management.ManagementFactory;
import java.util.List;

public class UptimeCommand extends BaseCommand {

    public UptimeCommand() {
        super("prismautils.command.uptime", true, "/uptime");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            return false;
        }
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

        String uptimeReadable = TextUtils.formatDuration(uptime);
        sender.sendMessage("Server uptime: " + uptimeReadable);
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}
