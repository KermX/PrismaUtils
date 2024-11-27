package me.kermx.prismaUtils.Commands.OtherCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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


        Player playerSender = (Player) sender;
        if (args.length == 0) {
            if (!playerSender.hasPermission("prismautils.command.feed")) {
                playerSender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
                return true;
            }
            feedPlayer(playerSender);
            playerSender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().feedMessage));
            return true;
        }

        if (args.length == 1) {
            String targetName = args[0];
            if (targetName.equalsIgnoreCase("all")) {
                if (!sender.hasPermission("prismautils.command.feed.all")) {
                    playerSender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
                    return true;
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    feedPlayer(player);
                }
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().feedAllMessage));
            } else {
                Player target = Bukkit.getPlayerExact(targetName);
                if (target == null) {
                    sender.sendMessage("Player \"" + targetName + "\" is not online!");
                    return true;
                }
                if (!sender.hasPermission("prismautils.command.feed.others")) {
                    playerSender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
                    return true;
                }
                feedPlayer(target);

                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().feedOtherMessage,
                        Placeholder.component("target", target.displayName())));
                target.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().feedFedByOtherMessage,
                        Placeholder.component("source", sender.name())));
//                sender.sendMessage("You have refilled " + target.getName() + "'s hunger!");
//                target.sendMessage("Your hunger has been refilled by " + sender.getName() + "!");
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
