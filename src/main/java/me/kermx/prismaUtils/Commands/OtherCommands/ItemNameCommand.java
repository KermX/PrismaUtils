package me.kermx.prismaUtils.Commands.OtherCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemNameCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("prismautils.command.itemname")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().incorrectUsageMessage,
                    Placeholder.component("usage", Component.text(command.getUsage()))));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().itemNameInvalidItemMessage));
            return true;
        }

        String newName = String.join(" ", args);
        Component displayName = MiniMessage.miniMessage().deserialize(newName);

        item.editMeta(meta -> meta.displayName(displayName));

        player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().itemNameMessage,
                Placeholder.component("name", displayName)));

        return true;
    }
}
