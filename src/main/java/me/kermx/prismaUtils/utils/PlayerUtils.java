package me.kermx.prismaUtils.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PlayerUtils {
    private PlayerUtils() {
        throw new UnsupportedOperationException("Utility class (PlayerUtils) - cannot be instantiated");
    }

    /**
     * Gets an offline player by their name.
     *
     * @param name The name of the player.
     * @return The OfflinePlayer associated with the given name.
     */
    public static OfflinePlayer getOfflinePlayer(String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        return Bukkit.getOfflinePlayer(name);
    }

    /**
     * Gets an offline player by their UUID.
     *
     * @param uuid The UUID of the player.
     * @return The OfflinePlayer associated with the given UUID.
     */
    public static OfflinePlayer getOfflinePlayer(UUID uuid) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        return Bukkit.getOfflinePlayer(uuid);
    }

    /**
     * Gets an online player by their exact name.
     *
     * @param name The exact name of the player.
     * @return The Player if found online, or null if not found.
     */
    public static Player getOnlinePlayer(String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        return Bukkit.getPlayerExact(name);
    }

    /**
     * Gets an online player by their UUID.
     *
     * @param uuid The UUID of the player.
     * @return The Player if found online, or null if not found.
     */
    public static Player getOnlinePlayer(UUID uuid) {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Gets the names of all online players.
     *
     * @return A list of online player names.
     */
    public static List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the player's main inventory (the first 36 slots).
     *
     * @param player The player whose inventory is being retrieved.
     * @return An array of ItemStacks representing the main inventory.
     */
    public static ItemStack[] getMainInventory(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        PlayerInventory inventory = player.getInventory();
        return Arrays.copyOf(inventory.getContents(), 36);
    }

    /**
     * Retrieves the player's hotbar items (the first 9 slots of the main inventory).
     *
     * @param player The player whose hotbar is being retrieved.
     * @return An array of ItemStacks representing the hotbar.
     */
    public static ItemStack[] getHotbar(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        PlayerInventory inventory = player.getInventory();
        return Arrays.copyOfRange(inventory.getContents(), 0, 9);
    }

    /**
     * Retrieves the player's armor contents.
     *
     * @param player The player whose armor is being retrieved.
     * @return An array of ItemStacks representing the player's armor.
     */
    public static ItemStack[] getArmorContents(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return player.getInventory().getArmorContents();
    }

}
