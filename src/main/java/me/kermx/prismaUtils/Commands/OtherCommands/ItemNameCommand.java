package me.kermx.prismaUtils.Commands.OtherCommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemNameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (args.length == 0){
            sender.sendMessage("Usage: /itemname <name>");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("prismautils.command.itemname")){
            player.sendMessage("You do not have permission to use this command!");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR){
            player.sendMessage("You must be holding an item to use this command!");
            return true;
        }

        String newName = String.join(" ", args);
        Component displayName = MiniMessage.miniMessage().deserialize(newName);

        item.editMeta(meta -> meta.displayName(displayName));
        player.sendMessage("Item name set to: " + newName);
        return true;
    }
}
