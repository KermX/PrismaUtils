package me.kermx.prismaUtils.commands.player;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BottomCommand extends BaseCommand {

    public BottomCommand() {
        super("prismautils.command.bottom", false, "/bottom");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            return false;
        }

        Player player = (Player) sender;
        Location currentLocation = player.getLocation();
        int x = currentLocation.getBlockX();
        int z = currentLocation.getBlockZ();
        int minY = currentLocation.getWorld().getMinHeight();
        int maxY = currentLocation.getWorld().getMaxHeight();

        boolean foundSafeLocation = false;
        Location targetLocation = null;

        for (int y = minY; y < maxY - 1; y++) {
            Location blockLocation = new Location(currentLocation.getWorld(), x, y, z);
            Location aboveBlock1 = blockLocation.clone().add(0, 1, 0);
            Location aboveBlock2 = blockLocation.clone().add(0, 2, 0);

            if (blockLocation.getBlock().getType().isSolid() // Ensure it's a solid block
                    && (aboveBlock1.getBlock().getType() == Material.AIR || aboveBlock1.getBlock().getType() == Material.CAVE_AIR)
                    && (aboveBlock2.getBlock().getType() == Material.AIR || aboveBlock2.getBlock().getType() == Material.CAVE_AIR)) {

                targetLocation = blockLocation.add(0.5, 1, 0.5); // Calculate target teleport location
                targetLocation.setPitch(player.getPitch());
                targetLocation.setYaw(player.getYaw());
                foundSafeLocation = true;
                break;
            }
        }

        if (foundSafeLocation) {
            // Check if player is already at the target location
            if (currentLocation.getBlockX() == targetLocation.getBlockX() && currentLocation.getBlockZ() == targetLocation.getBlockZ() && currentLocation.getBlockY() == targetLocation.getBlockY()) {
                player.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().bottomMessageInvalidBlock)
                );
            } else {
                player.teleport(targetLocation);
                player.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().bottomMessage)
                );
            }
        } else {
            player.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().bottomMessageInvalidBlock)
            );
        }

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}
