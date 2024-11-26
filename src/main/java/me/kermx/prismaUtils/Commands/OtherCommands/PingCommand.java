package me.kermx.prismaUtils.Commands.OtherCommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("prismautils.command.ping")){
            sender.sendMessage("You do not have permission to use this command!");
            return true;
        }

        if (args.length == 0){
            if (sender instanceof Player player){
                int ping = player.getPing();

                player.sendMessage("Your ping is: " + ping + "ms");
            } else {
                sender.sendMessage("Only players can use this command!");
            }
        } else if (args.length == 1) {
            if (sender.hasPermission("prismautils.command.ping.others")){
                Player target = Bukkit.getPlayer(args[0]);

                if (target != null && target.isOnline()) {
                    int ping = target.getPing();
                    sender.sendMessage(target.getName() + "'s ping is: " + ping + " ms");
                } else {
                    sender.sendMessage("Player not found or is offline.");
                }
            } else {
                sender.sendMessage("You do not have permission to check others' ping.");
            }
        } else {
            sender.sendMessage("Usage: /ping [player]");
        }

        return true;
    }
}
