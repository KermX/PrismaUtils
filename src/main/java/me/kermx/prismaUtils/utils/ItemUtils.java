package me.kermx.prismaUtils.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Objects;

public final class ItemUtils {
    private ItemUtils() {
        throw new UnsupportedOperationException("Utility class (ItemUtils) - cannot be instantiated");
    }

    /**
     * Gives an ItemStack to the player. If the player's inventory is full,
     * any leftover items will be dropped at the player's location.
     *
     * @param player the player to receive the item; must not be null
     * @param item   the ItemStack to give; must not be null
     * @throws NullPointerException if player or item is null
     */
    public static void giveItems(Player player, ItemStack item) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(item, "ItemStack cannot be null");

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
     * Checks if the provided string corresponds to a valid Material.
     *
     * @param itemString the Material name (or alias) to check; may be null or empty
     * @return true if the material exists, false otherwise
     */
    public static boolean isValidItemString(String itemString) {
        if (itemString == null || itemString.isEmpty()) {
            return false;
        }
        Material material = Material.matchMaterial(itemString);
        return material != null;
    }

    /**
     * Checks if the given ItemStack has special metadata (a custom display name or lore).
     *
     * @param item the item to check; may be null
     * @return true if the item has custom display name or lore, false otherwise
     */
    public static boolean itemHasSpecialMeta(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && (meta.hasDisplayName() || meta.hasLore());
    }

    /**
     * Checks if the given ItemStack contains all specified enchantments.
     *
     * @param item         the item to check; may be null
     * @param enchantments one or more enchantments to look for; must not be null
     * @return true if the item contains all provided enchantments, false otherwise
     * @throws NullPointerException if enchantments is null
     */
    public static boolean itemHasEnchantments(ItemStack item, Enchantment... enchantments) {
        Objects.requireNonNull(enchantments, "Enchantments cannot be null");
        if (item == null) {
            return false;
        }
        for (Enchantment enchantment : enchantments) {
            if (!item.containsEnchantment(enchantment)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given ItemStack contains at least one of the specified enchantments.
     *
     * @param item         the item to check; may be null
     * @param enchantments one or more enchantments to check; must not be null
     * @return true if the item contains any one of the provided enchantments, false otherwise
     * @throws NullPointerException if enchantments is null
     */
    public static boolean itemHasAnyEnchantment(ItemStack item, Enchantment... enchantments) {
        Objects.requireNonNull(enchantments, "Enchantments cannot be null");
        if (item == null) {
            return false;
        }
        for (Enchantment enchantment : enchantments) {
            if (item.containsEnchantment(enchantment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given ItemStack is tagged with the specified tag.
     *
     * @param item the item to check; may be null
     * @param tag  the tag to check for; must not be null
     * @return true if the item's material is in the provided tag, false otherwise
     * @throws NullPointerException if tag is null
     */
    public static boolean itemIsTagged(ItemStack item, Tag<Material> tag) {
        Objects.requireNonNull(tag, "Tag cannot be null");
        if (item == null) {
            return false;
        }
        return tag.isTagged(item.getType());
    }
}
