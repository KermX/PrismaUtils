package me.kermx.prismaUtils.Commands.player;

import me.kermx.prismaUtils.Commands.BaseCommand;
import me.kermx.prismaUtils.managers.ConfigManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BottomCommand extends BaseCommand {

    public BottomCommand(){
        super("prismautils.command.bottom", false, "/bottom");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length > 0){
            return false;
        }
        Player player = (Player) sender;
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
                player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().bottomMessage));
                return true;
            }
        }
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        return super.onTabCompleteExecute(sender, args);
    }

// Add to show when the player does not have a valid block below them
//        player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().bottomMessageInvalidBlock));
//        return true;

}
