package me.kermx.prismaUtils.Commands.AdminCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SetModelDataCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!sender.hasPermission("prismautils.command.setmodeldata")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            return true;
        }

        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("You must hold an item in your hand to use this command!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("Usage: /setmodeldata <modeldata>");
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
}
