package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.BlockUtils;
import me.kermx.prismaUtils.utils.ItemUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MassDisenchantCommand extends BaseCommand {

    public MassDisenchantCommand() {
        super("prismautils.command.disenchant", false, "/disenchant");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        Block targetBlock = player.getTargetBlockExact(5);

        if (targetBlock == null) {
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().disenchantNoBlockMessage)
            );
            return true;
        }
        if (BlockUtils.blockIsProtected(player, targetBlock)) {
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().blockProtectedMessage)
            );
            return true;
        }

        Inventory blockInventory = BlockUtils.getInventoryFromBlock(targetBlock);
        if (blockInventory == null) {
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().disenchantNoBlockMessage)
            );
            return true;
        }

        Material targetMaterial = null;
        if (args.length > 0) {
            try {
                targetMaterial = Material.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().invalidMaterialMessage)
                );
                return true;
            }
        }

        int removedEnchantmentsCount = 0;
        for (ItemStack item : blockInventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            if (targetMaterial != null && item.getType() != targetMaterial) {
                continue;
            }
            removedEnchantmentsCount += ItemUtils.removeAllEnchantments(item);
        }

        if (removedEnchantmentsCount > 0) {
            player.giveExp(removedEnchantmentsCount * ConfigManager.getInstance().getMainConfig().disenchantCommandExpPerEnchantment);
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().disenchantSuccessMessage,
                            Placeholder.component("amount", Component.text(removedEnchantmentsCount)))
            );
        } else if (targetMaterial != null) {
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().disenchantNoEnchantmentsMessage)
            );
        } else {
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().disenchantNoEnchantmentsMessage)
            );
        }
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> materials = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (Material mat : Material.values()) {
                if (mat.name().toLowerCase().startsWith(input)) {
                    materials.add(mat.name().toLowerCase());
                }
            }
            return materials;
        }
        return super.onTabCompleteExecute(sender, args);
    }
}