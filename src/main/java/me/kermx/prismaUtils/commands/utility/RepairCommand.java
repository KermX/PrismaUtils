package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RepairCommand extends BaseCommand {

    public RepairCommand() {
        super("prismautils.command.repair", false, "/repair <hand|all>");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length != 1 || (!args[0].equalsIgnoreCase("hand") && !args[0].equalsIgnoreCase("all"))) {
            return false;
        }

        if (args[0].equalsIgnoreCase("hand")) {
            repairHand((Player) sender);
        } else if (args[0].equalsIgnoreCase("all")) {
            repairAll((Player) sender);
        } else {
            return false;
        }
        return true;
    }

    private void repairHand(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().repairNoItemInHandMessage)
            );
            return;
        }

        ItemMeta meta = itemInHand.getItemMeta();
        if (meta instanceof Damageable damageable) {
            damageable.setDamage(0);
            itemInHand.setItemMeta(meta);
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().repairRepairedMessage)
            );
        } else {
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().repairInvalidItemMessage)
            );
        }
    }

    private void repairAll(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable damageable) {
                damageable.setDamage(0);
                item.setItemMeta(meta);
            }
        }
        player.sendMessage(
                TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().repairAllRepairedMessage)
        );
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partialArg = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();

            if ("hand".startsWith(partialArg)) {
                suggestions.add("hand");
            }
            if ("all".startsWith(partialArg)) {
                suggestions.add("all");
            }
            return suggestions;
        }
        return super.onTabCompleteExecute(sender, args);
    }
}
