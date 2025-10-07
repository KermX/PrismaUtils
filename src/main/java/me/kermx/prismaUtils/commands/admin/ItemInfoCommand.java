package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Map;

public class ItemInfoCommand extends BaseCommand {

    public ItemInfoCommand() {
        super("prismautils.command.iteminfo", false, "/iteminfo");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            return false;
        }
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();


        if (item != null && item.getType() != Material.AIR) {
            player.sendMessage(Component.text("Item Info:", NamedTextColor.GREEN));

            // Basic item info
            player.sendMessage(Component.text("Type: ", NamedTextColor.YELLOW));
            TextUtils.sendCopyableMessage(player, item.getType().name(), item.getType().name());

            player.sendMessage(Component.text("Amount: ", NamedTextColor.YELLOW));
            TextUtils.sendCopyableMessage(player, String.valueOf(item.getAmount()), String.valueOf(item.getAmount()));

            player.sendMessage(Component.text("Max Stack Size: ", NamedTextColor.YELLOW));
            TextUtils.sendCopyableMessage(player, String.valueOf(item.getMaxStackSize()), String.valueOf(item.getMaxStackSize()));

            player.sendMessage(Component.text("Durability: ", NamedTextColor.YELLOW));
            TextUtils.sendCopyableMessage(player, String.valueOf(item.getDurability()), String.valueOf(item.getDurability()));

            // Item meta data
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {

                player.sendMessage(Component.text("Item Meta Data:", NamedTextColor.YELLOW));
                TextUtils.sendCopyableMessage(player, meta.toString(), meta.toString());

                if (meta.hasDisplayName()) {
                    player.sendMessage(Component.text("Display Name: ", NamedTextColor.YELLOW));
                    TextUtils.sendCopyableMessage(player, meta.displayName(), meta.displayName());
                }
                if (meta.hasLore()) {
                    player.sendMessage(Component.text("Lore: ", NamedTextColor.YELLOW));
                    String lore = String.join(", ", meta.getLore());
                    TextUtils.sendCopyableMessage(player, lore, lore);
                }
                if (meta.hasEnchants()) {
                    player.sendMessage(Component.text("Enchantments:", NamedTextColor.YELLOW));
                    for (Map.Entry<Enchantment, Integer> enchant : meta.getEnchants().entrySet()) {
                        String enchantData = enchant.getKey().getKey().getKey() + " Level: " + enchant.getValue();
                        player.sendMessage(Component.text("  ", NamedTextColor.YELLOW));
                        TextUtils.sendCopyableMessage(player, enchantData, enchantData);
                    }
                }
                if (meta.hasCustomModelData()) {
                    player.sendMessage(Component.text("Custom Model Data: ", NamedTextColor.YELLOW));
                    TextUtils.sendCopyableMessage(player, String.valueOf(meta.getCustomModelData()), String.valueOf(meta.getCustomModelData()));
                }
                if (meta.hasAttributeModifiers()) {
                    player.sendMessage(Component.text("Attributes: ", NamedTextColor.YELLOW));
                    TextUtils.sendCopyableMessage(player, meta.getAttributeModifiers().toString(), meta.getAttributeModifiers().toString());
                }
                if (meta instanceof org.bukkit.inventory.meta.PotionMeta potionMeta) {
                    for (PotionEffect effect : potionMeta.getCustomEffects()) {
                        String effectData = effect.getType().getName() + " Duration: " + effect.getDuration() + " Amplifier: " + effect.getAmplifier();
                        player.sendMessage(Component.text("Potion Effect: ", NamedTextColor.YELLOW));
                        TextUtils.sendCopyableMessage(player, effectData, effectData);
                    }
                }
            }
        } else {
            player.sendMessage(Component.text("You must be holding an item to use this command!", NamedTextColor.RED));
        }
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}