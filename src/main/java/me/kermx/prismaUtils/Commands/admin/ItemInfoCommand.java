package me.kermx.prismaUtils.Commands.admin;

import me.kermx.prismaUtils.Commands.BaseCommand;
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

    public ItemInfoCommand(){
        super("prismautils.command.iteminfo", false, "/iteminfo");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length > 0){
            return false;
        }
        Player player = (Player) sender;
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
                if (meta instanceof org.bukkit.inventory.meta.PotionMeta potionMeta) {
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

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        return super.onTabCompleteExecute(sender, args);
    }
}
