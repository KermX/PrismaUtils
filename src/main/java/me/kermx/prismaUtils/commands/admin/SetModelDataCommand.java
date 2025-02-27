package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.BaseCommand;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SetModelDataCommand extends BaseCommand {

    public SetModelDataCommand() {
        super("prismautils.command.setmodeldata", false, "/setmodeldata <modeldata>");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length != 1) {
            return false;
        }
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("You must hold an item in your hand to use this command!");
            return true;
        }
        try {
            int modelData = Integer.parseInt(args[0]);

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                player.sendMessage("This item does not have metadata!");
                return true;
            }

            meta.setCustomModelData(modelData);
            item.setItemMeta(meta);

            player.sendMessage("Model data set to " + modelData + "!");
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid model data! Must be a number!");
        }
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}