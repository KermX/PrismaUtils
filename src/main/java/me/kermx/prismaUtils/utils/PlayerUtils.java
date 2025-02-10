package me.kermx.prismaUtils.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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

}
