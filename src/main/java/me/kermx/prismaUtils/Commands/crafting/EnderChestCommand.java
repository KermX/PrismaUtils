package me.kermx.prismaUtils.Commands.crafting;

import me.kermx.prismaUtils.Commands.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class EnderChestCommand extends BaseCommand {

    public EnderChestCommand(){
        super("prismautils.command.enderchest", false, "/enderchest");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length > 0){
            return false;
        }
        Player player = (Player) sender;
        Inventory enderChest = player.getEnderChest();
        player.openInventory(enderChest);
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        return super.onTabCompleteExecute(sender, args);
    }
}
