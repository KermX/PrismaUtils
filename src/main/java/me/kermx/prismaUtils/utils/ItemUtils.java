package me.kermx.prismaUtils.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ItemUtils {
    private ItemUtils() {
        throw new UnsupportedOperationException("Utility class (ItemUtils) - cannot be instantiated");
    }

    /**
     * Gives the player a specified amount of items.
     *
     * @param player   the player to receive the item; must not be null
     * @param material the item to give; must not be null
     * @param amount   the amount of items to give
     * @throws NullPointerException if player or item is null
     */
    public static void giveItems(Player player, Material material, int amount) {
        if (amount <= 0) {
            return;
        }
        giveItems(player, new ItemStack(material, amount));
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
     * Damages the item by one durability point.
     *
     * @param item the item to damage; must not be null
     * @throws NullPointerException if item is null
     */
    public static void damageItem(ItemStack item, int damage) {
        Objects.requireNonNull(item, "ItemStack cannot be null");

        if (!(item.getItemMeta() instanceof Damageable damageableMeta)) {
            return;
        }

        int newDamage = damageableMeta.getDamage() + damage;
        int maxDurability = item.getType().getMaxDurability();

        if (newDamage >= maxDurability) {
            item.setAmount(0);
        } else {
            damageableMeta.setDamage(newDamage);
            item.setItemMeta(damageableMeta);
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
     * Removes all enchantments from the given ItemStack.
     *
     * @param item the item to remove enchantments from; may be null
     * @return the number of enchantments removed
     */
    public static int removeAllEnchantments(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return 0;
        }
        int removedCount = 0;

        if (item.getType() == Material.ENCHANTED_BOOK) {
            if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
                removedCount = meta.getStoredEnchants().size();
            }
            item.setType(Material.BOOK);
        } else {
            Map<Enchantment, Integer> currentEnchants = new HashMap<>(item.getEnchantments());
            for (Enchantment ench : currentEnchants.keySet()) {
                item.removeEnchantment(ench);
                removedCount++;
            }
        }
        return removedCount;
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

    /**
     * Checks if the given ItemStack has the specified ItemFlag.
     *
     * @param item the item to check; may be null
     * @param flag the ItemFlag to check for; must not be null
     * @return true if the item has the specified flag, false otherwise
     * @throws NullPointerException if flag is null
     */
    public static boolean itemHasFlag(ItemStack item, ItemFlag flag) {
        Objects.requireNonNull(flag, "ItemFlag cannot be null");
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasItemFlag(flag);
    }

    /**
     * Counts items in an array that match a certain material.
     *
     * @param items    the array of items to count; must not be null
     * @param material the material to count; must not be null
     * @return the number of items that match the material
     */
    public static int countItems(ItemStack[] items, Material material) {
        int count = 0;
        for (ItemStack item : items) {
            if (item != null && item.getType() == material) {
                if (!itemHasSpecialMeta(item)) {
                    count += item.getAmount();
                }
            }
        }
        return count;
    }

    /**
     * Merges multiple ItemStacks of the same type into a single stack (up to the maximum stack size).
     *
     * @param stacks the ItemStacks to merge; must not be null
     * @return a new ItemStack representing the merged items, or null if the input array is empty
     */
    public static ItemStack mergeItemStacks(ItemStack... stacks) {
        if (stacks == null || stacks.length == 0) return null;
        ItemStack base = null;
        int totalAmount = 0;
        for (ItemStack stack : stacks) {
            if (stack == null) continue;
            if (base == null) {
                base = stack.clone();
            } else if (!stack.isSimilar(base)) {
                continue; // Skip items that are not similar.
            }
            totalAmount += stack.getAmount();
        }
        if (base != null) {
            int maxStack = base.getMaxStackSize();
            base.setAmount(Math.min(totalAmount, maxStack));
        }
        return base;
    }
}