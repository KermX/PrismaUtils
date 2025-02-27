package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemNameCommand extends BaseCommand {

    public ItemNameCommand() {
        super("prismautils.command.itemname", false, "/itemname");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {

            sender.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().itemNameInvalidItemMessage)
            );

            return false;
        }

        String newName = String.join(" ", args);
        Component displayName = MiniMessage.miniMessage().deserialize(newName);

        item.editMeta(meta -> meta.displayName(displayName));

        sender.sendMessage(
                TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().itemNameMessage,
                        Placeholder.component("name", displayName))
        );

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}