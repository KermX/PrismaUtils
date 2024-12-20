package me.kermx.prismaUtils.Commands.AdminCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.Map;

public class ItemInfoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("prismautils.command.iteminfo")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.getType() != Material.AIR) {
            player.sendMessage("Item Info:");
            player.sendMessage("Type: " + item.getType().name());
            player.sendMessage("Amount: " + item.getAmount());
            player.sendMessage("Max Stack Size: " + item.getMaxStackSize());
            player.sendMessage("Durability: " + item.getDurability());

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName()) {
                    player.sendMessage("Display Name: " + meta.getDisplayName());
                }
                if (meta.hasLore()) {
                    player.sendMessage("Lore: " + String.join(", ", meta.getLore()));
                }
                if (meta.hasEnchants()) {
                    player.sendMessage("Enchantments:");
                    for (Map.Entry<Enchantment, Integer> enchant : meta.getEnchants().entrySet()) {
                        player.sendMessage("  " + enchant.getKey().getKey().getKey() + " Level: " + enchant.getValue());
                    }
                }
                if (meta.hasCustomModelData()) {
                    player.sendMessage("Custom Model Data: " + meta.getCustomModelData());
                }
                if (meta.hasAttributeModifiers()) {
                    player.sendMessage("Attributes: " + meta.getAttributeModifiers().toString());
                }
                if (meta instanceof org.bukkit.inventory.meta.PotionMeta) {
                    org.bukkit.inventory.meta.PotionMeta potionMeta = (org.bukkit.inventory.meta.PotionMeta) meta;
                    for (PotionEffect effect : potionMeta.getCustomEffects()) {
                        player.sendMessage("Potion Effect: " + effect.getType().getName() + " Duration: " + effect.getDuration() + " Amplifier: " + effect.getAmplifier());
                    }
                }
            }
        } else {
            player.sendMessage("You must be holding an item to use this command!");
        }

        return true;
    }
}
