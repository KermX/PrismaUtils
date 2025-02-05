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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Location currentLocation = player.getLocation();
        int x = currentLocation.getBlockX();
        int z = currentLocation.getBlockZ();
        int minY = currentLocation.getWorld().getMinHeight();
        int maxY = currentLocation.getWorld().getMaxHeight();

        for (int y = minY; y < maxY - 1; y++) {
            Location blockLocation = new Location(currentLocation.getWorld(), x, y, z);
            Location aboveBlock1 = blockLocation.clone().add(0, 1, 0);
            Location aboveBlock2 = blockLocation.clone().add(0, 2, 0);

            if (blockLocation.getBlock().getType().isSolid() // Ensure it's a solid block
                    && (aboveBlock1.getBlock().getType() == Material.AIR || aboveBlock1.getBlock().getType() == Material.CAVE_AIR)
                    && (aboveBlock2.getBlock().getType() == Material.AIR || aboveBlock2.getBlock().getType() == Material.CAVE_AIR)) {

                player.teleport(blockLocation.add(0.5, 1, 0.5));
                player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().bottomMessage));
                return true;
            }
        }

        player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().bottomMessageInvalidBlock));
        return true;
    }
}
