package me.kermx.prismaUtils.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PlayerUtils {
    private PlayerUtils(){}

    /**
     * Get an offline player by their name
     * @param name The name of the player
     * @return The offline player
     */
    public static OfflinePlayer getOfflinePlayer(String name){
        return Bukkit.getOfflinePlayer(name);
    }

}
