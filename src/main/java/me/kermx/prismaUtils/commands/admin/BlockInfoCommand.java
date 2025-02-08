package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.BaseCommand;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BlockInfoCommand extends BaseCommand {

    public BlockInfoCommand(){
        super("prismautils.command.blockinfo", false, "/blockinfo");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length > 0){
            return false;
        }
        Player player = (Player) sender;
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

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        return super.onTabCompleteExecute(sender, args);
    }
}
