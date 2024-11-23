package me.kermx.prismaUtils.Commands.CraftingStationCommands;


import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AnvilCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("prismautils.command.anvil")){
            player.sendMessage("You do not have permission to use this command! DUMMY");
            return true;
        }

        Location location = player.getLocation();
        player.openAnvil(location, true);
        return true;
    }
}
