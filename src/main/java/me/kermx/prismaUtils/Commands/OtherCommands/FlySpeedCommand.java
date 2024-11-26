package me.kermx.prismaUtils.Commands.OtherCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlySpeedCommand implements CommandExecutor {

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
}
