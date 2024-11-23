package me.kermx.prismaUtils.Commands.OtherCommands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NearCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("prismautils.command.near")){
            player.sendMessage("You do not have permission to use this command!");
            return true;
        }

        Location location = player.getLocation();
        double radius = 100;

        if (args.length > 0){
            try {
                radius = Double.parseDouble(args[0]);
            } catch (NumberFormatException e){
                player.sendMessage("Invalid radius!");
                return true;
            }
        }

        player.sendMessage("Players withing " + radius + " blocks:");
        boolean found = false;
        for (Player onlinePlayer : player.getWorld().getPlayers()){
            if (onlinePlayer.getLocation().distance(location) <= radius && onlinePlayer != player){
                player.sendMessage(onlinePlayer.getName() + " is " + onlinePlayer.getLocation().distance(location) + " blocks away.");
                found = true;
            }
        }

        if (!found){
            player.sendMessage("No players found within " + radius + " blocks.");
        }
        return true;
    }
}
