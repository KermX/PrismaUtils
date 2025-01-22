package me.kermx.prismaUtils.Commands.CraftingStationCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class EnderChestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("prismautils.command.enderchest")){
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            return true;
        }

        Inventory enderChest = player.getEnderChest();
        player.openInventory(enderChest);
        return true;
    }

}
