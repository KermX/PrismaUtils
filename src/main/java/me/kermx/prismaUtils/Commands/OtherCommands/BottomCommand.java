package me.kermx.prismaUtils.Commands.OtherCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BottomCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;
        Location currentLocation = player.getLocation();
        int x = currentLocation.getBlockX();
        int z = currentLocation.getBlockZ();
        int minY = currentLocation.getWorld().getMinHeight();

        for (int y = minY; y <= currentLocation.getWorld().getMaxHeight(); y++) {
            Location blockLocation = new Location(currentLocation.getWorld(), x, y, z);
            if (blockLocation.getBlock().getType() != Material.AIR) {
                player.teleport(blockLocation.add(0.5, 1, 0.5));
                player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().bottomMessage));
                return true;
            }
        }

        player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().bottomMessageInvalidBlock));
        return true;
    }
}
