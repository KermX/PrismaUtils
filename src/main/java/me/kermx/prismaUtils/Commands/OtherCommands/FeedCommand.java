package me.kermx.prismaUtils.Commands.OtherCommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FeedCommand implements CommandExecutor, TabCompleter {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && args.length == 0) {
            sender.sendMessage("You must specify a player name or use \"all\" when using this command from the console!");
            return true;
        }

        if (args.length == 0) {
            Player player = (Player) sender;
            if (!player.hasPermission("prismautils.command.feed")) {
                player.sendMessage("You don't have permission to use this command!");
                return true;
            }
            feedPlayer(player);
            player.sendMessage("Your hunger has been refilled!");
            return true;
        }

        if (args.length == 1) {
            String targetName = args[0];
            if (targetName.equalsIgnoreCase("all")) {
                if (!sender.hasPermission("prismautils.command.feed.all")) {
                    sender.sendMessage("You don't have permission to feed all players!");
                    return true;
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    feedPlayer(player);
                }
                sender.sendMessage("All players' hunger has been refilled!");
            } else {
                Player target = Bukkit.getPlayerExact(targetName);
                if (target == null) {
                    sender.sendMessage("Player \"" + targetName + "\" is not online!");
                    return true;
                }
                if (!sender.hasPermission("prismautils.command.feed.others")) {
                    sender.sendMessage("You don't have permission to feed other players!");
                    return true;
                }
                feedPlayer(target);
                sender.sendMessage("You have refilled " + target.getName() + "'s hunger!");
                target.sendMessage("Your hunger has been refilled by " + sender.getName() + "!");
            }
            return true;
        }

        sender.sendMessage("Usage: /feed [player|all]");
        return true;
    }

    private void feedPlayer(Player player) {
        player.setFoodLevel(20);
        player.setSaturation(20.0F); // Max saturation
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partialArg = args[0].toLowerCase(); // The partially typed argument
            if ("all".startsWith(partialArg)) {
                completions.add("all");
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partialArg)) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }
}
