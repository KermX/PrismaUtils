package me.kermx.prismaUtils.Commands.OtherCommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FlySpeedCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("prismautils.command.flyspeed")) {
            player.sendMessage("You do not have permission to use this command!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("Usage: /flyspeed <speed>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            player.setFlySpeed(0.1f);
            player.sendMessage("Fly speed reset to default.");
            return true;
        }

        try {
            float speed = Float.parseFloat(args[0]);
            if (speed < 0 || speed > 10){
                player.sendMessage("Invalid speed! Must be between 0 and 10.");
            }
            float adjustedSpeed = speed / 10.0f;
            player.setFlySpeed(adjustedSpeed);
            player.sendMessage("Fly speed set to " + speed + ".");

        } catch (NumberFormatException e) {
            player.sendMessage("Invalid speed. Enter a number between 0 and 10 or 'reset'.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partialArg = args[0].toLowerCase();

            // Suggest "reset" if it matches the partial argument
            if ("reset".startsWith(partialArg)) {
                completions.add("reset");
            }

            // Suggest numbers from 0 to 10 that start with the partial argument
            for (int i = 0; i <= 10; i++) {
                String speedStr = String.valueOf(i);
                if (speedStr.startsWith(partialArg)) {
                    completions.add(speedStr);
                }
            }
        }
        return completions;
    }
}
