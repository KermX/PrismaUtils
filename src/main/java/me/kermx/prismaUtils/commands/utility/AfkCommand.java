package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.feature.AfkManager;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AfkCommand extends BaseCommand {

    private final AfkManager afkManager;

    public AfkCommand(AfkManager afkManager) {
        super("prismautils.command.afk", false, "/afk [player]");
        this.afkManager = afkManager;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length > 0 && sender.hasPermission("prismautils.command.afk.others")) {
            // Set AFK status for another player
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(TextUtils.deserializeString("<red>Player not found.</red>"));
                return true;
            }

            boolean currentAfkStatus = afkManager.isAfk(target.getUniqueId());
            afkManager.setAfk(target, !currentAfkStatus);

            String status = !currentAfkStatus ? "AFK" : "no longer AFK";
            sender.sendMessage(TextUtils.deserializeString("<green>Set " + target.getName() + " as " + status + ".</green>"));
            return true;
        } else {
            // Toggle own AFK status
            Player player = (Player) sender;
            boolean currentAfkStatus = afkManager.isAfk(player.getUniqueId());
            afkManager.setAfk(player, !currentAfkStatus);
            return true;
        }
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("prismautils.command.afk.others")) {
            return PlayerUtils.getOnlinePlayerNamesStartingWith(args[0]);
        }
        return super.onTabCompleteExecute(sender, args);
    }
}
