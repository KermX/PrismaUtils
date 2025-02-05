package me.kermx.prismaUtils.Commands.AdminCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockInfoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("prismautils.command.blockinfo")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            return true;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock != null && targetBlock.getType() != Material.AIR) {
            player.sendMessage("Block Info:");
            player.sendMessage("Type: " + targetBlock.getType().name());
            player.sendMessage("Hardness: " + targetBlock.getType().getHardness());
            player.sendMessage("Blast Resistance: " + targetBlock.getType().getBlastResistance());
            player.sendMessage("Location: " + targetBlock.getLocation());
            player.sendMessage("Data: " + targetBlock.getBlockData().getAsString());
        } else {
            player.sendMessage("You must be looking at a block to use this command!");
        }

        return true;
    }
}
