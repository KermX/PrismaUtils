package me.kermx.prismaUtils.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ItemUtils {
    private ItemUtils() {}

    /**
     * Give an item to a player
     * @param player The player to give the item to
     * @param item The item to give
     */
    public static void giveItems(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(item);

        if (!remainingItems.isEmpty()) {
            Location location = player.getLocation();
            for (ItemStack remainingItem : remainingItems.values()) {
                if (remainingItem != null) {
                    Item droppedItem = player.getWorld().dropItem(location, remainingItem);
                    droppedItem.setPickupDelay(0);
                }
            }
        }
    }
    /**
     * Check if an item is valid
     * @param itemString The Material name of the item to check
     * @return true if the item is valid, false otherwise
     */
    public static boolean isValidItemString(String itemString){
        Material material = Material.matchMaterial(itemString);
        return material != null;
    }
    /**
     * Check if an item has a special meta (display name or lore)
     * @param item The item to check
     * @return true if the item has a special meta, false otherwise
     */
    public static boolean itemHasSpecialMeta(ItemStack item){
        return item.getItemMeta() != null && (item.getItemMeta().hasDisplayName() || item.getItemMeta().hasLore());
    }
    /**
     * Check if an item has a specific enchantment
     * @param item The item to check
     * @param enchantments The enchantments to check for
     * @return true if the item has all the enchantments, false otherwise
     */
    public static boolean itemHasEnchantments(ItemStack item, Enchantment... enchantments){
        if (item == null) return false;
        for (Enchantment enchantment : enchantments) {
            if (!item.containsEnchantment(enchantment)) {
                return false;
            }
        }
        return true;
    }
    /**
     * Check if an item has any of the specified enchantments
     * @param item The item to check
     * @param enchantments The enchantments to check for
     * @return true if the item has any of the enchantments, false otherwise
     */
    public static boolean itemHasAnyEnchantment(ItemStack item, Enchantment... enchantments) {
        if (item == null) return false;
        for (Enchantment enchantment : enchantments) {
            if (item.containsEnchantment(enchantment)) {
                return true; // found one match
            }
        }
        return false;
    }
    /**
     * Check if an item is tagged with a specific tag
     * @param item The item to check
     * @param tag The tag to check for
     * @return true if the item is tagged with the tag, false otherwise
     */
    public static boolean itemIsTagged(ItemStack item, Tag<Material> tag){
        if (item == null) return false;
        return tag.isTagged(item.getType());
    }
}
