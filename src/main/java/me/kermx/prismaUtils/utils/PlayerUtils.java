package me.kermx.prismaUtils.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class PlayerUtils {
    private PlayerUtils(){
        throw new UnsupportedOperationException("Utility class (PlayerUtils) - cannot be instantiated");
    }

    /**
     * Get an offline player by their name
     * @param name The name of the player
     * @return The offline player
     */
    public static OfflinePlayer getOfflinePlayer(String name){
        return Bukkit.getOfflinePlayer(name);
    }

    public static ItemStack[] getMainInventory(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] mainInventory = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            mainInventory[i] = inventory.getItem(i);
        }
        return mainInventory;
    }

}
