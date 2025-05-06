package me.kermx.prismaUtils.managers.general;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private static CooldownManager instance;

    // Default cooldown values in seconds
    private final int defaultHomeCooldownSeconds = 10;
    private final int defaultWarpCooldownSeconds = 15;
    private final int defaultBackCooldownSeconds = 30;

    // Maps to store cooldown timestamps for each player and command type
    private final Map<UUID, Long> homeCooldowns = new HashMap<>();
    private final Map<UUID, Long> warpCooldowns = new HashMap<>();
    private final Map<UUID, Long> backCooldowns = new HashMap<>();

    // Permission prefixes
    private static final String BYPASS_PERMISSION = "prismautils.cooldown.bypass";
    private static final String HOME_PERMISSION_PREFIX = "prismautils.cooldown.home.";
    private static final String WARP_PERMISSION_PREFIX = "prismautils.cooldown.warp.";
    private static final String BACK_PERMISSION_PREFIX = "prismautils.cooldown.back.";

    private CooldownManager() {
        // Private constructor for singleton
    }

    public static CooldownManager getInstance() {
        if (instance == null) {
            instance = new CooldownManager();
        }
        return instance;
    }

    /**
     * Check if a player can use the home teleport command
     * @param player The player to check
     * @return true if the player can teleport, false if the player is on cooldown
     */
    public boolean canUseHomeTeleport(Player player) {
        if (player.hasPermission(BYPASS_PERMISSION)) {
            return true;
        }

        int cooldownSeconds = getPlayerCooldown(player, HOME_PERMISSION_PREFIX, defaultHomeCooldownSeconds);
        return !isOnCooldown(player.getUniqueId(), homeCooldowns, cooldownSeconds);
    }

    /**
     * Check if a player can use the warp teleport command
     * @param player The player to check
     * @return true if the player can teleport, false if the player is on cooldown
     */
    public boolean canUseWarpTeleport(Player player) {
        if (player.hasPermission(BYPASS_PERMISSION)) {
            return true;
        }

        int cooldownSeconds = getPlayerCooldown(player, WARP_PERMISSION_PREFIX, defaultWarpCooldownSeconds);
        return !isOnCooldown(player.getUniqueId(), warpCooldowns, cooldownSeconds);
    }

    /**
     * Check if a player can use the back command
     * @param player The player to check
     * @return true if the player can teleport, false if the player is on cooldown
     */
    public boolean canUseBackCommand(Player player) {
        if (player.hasPermission(BYPASS_PERMISSION)) {
            return true;
        }

        int cooldownSeconds = getPlayerCooldown(player, BACK_PERMISSION_PREFIX, defaultBackCooldownSeconds);
        return !isOnCooldown(player.getUniqueId(), backCooldowns, cooldownSeconds);
    }

    /**
     * Set home teleport cooldown for a player
     * @param player The player to set cooldown for
     */
    public void setHomeTeleportCooldown(Player player) {
        if (!player.hasPermission(BYPASS_PERMISSION)) {
            homeCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    /**
     * Set warp teleport cooldown for a player
     * @param player The player to set cooldown for
     */
    public void setWarpTeleportCooldown(Player player) {
        if (!player.hasPermission(BYPASS_PERMISSION)) {
            warpCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    /**
     * Set back command cooldown for a player
     * @param player The player to set cooldown for
     */
    public void setBackCommandCooldown(Player player) {
        if (!player.hasPermission(BYPASS_PERMISSION)) {
            backCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    /**
     * Get the remaining cooldown time in seconds for home teleport
     * @param player The player to check
     * @return Remaining cooldown time in seconds
     */
    public int getHomeCooldownRemaining(Player player) {
        int cooldownSeconds = getPlayerCooldown(player, HOME_PERMISSION_PREFIX, defaultHomeCooldownSeconds);
        return getRemainingCooldown(player.getUniqueId(), homeCooldowns, cooldownSeconds);
    }

    /**
     * Get the remaining cooldown time in seconds for warp teleport
     * @param player The player to check
     * @return Remaining cooldown time in seconds
     */
    public int getWarpCooldownRemaining(Player player) {
        int cooldownSeconds = getPlayerCooldown(player, WARP_PERMISSION_PREFIX, defaultWarpCooldownSeconds);
        return getRemainingCooldown(player.getUniqueId(), warpCooldowns, cooldownSeconds);
    }

    /**
     * Get the remaining cooldown time in seconds for back command
     * @param player The player to check
     * @return Remaining cooldown time in seconds
     */
    public int getBackCooldownRemaining(Player player) {
        int cooldownSeconds = getPlayerCooldown(player, BACK_PERMISSION_PREFIX, defaultBackCooldownSeconds);
        return getRemainingCooldown(player.getUniqueId(), backCooldowns, cooldownSeconds);
    }

    /**
     * Gets the cooldown value for a player based on their permissions using
     * a more efficient approach similar to HomesCommand.getHomeLimit()
     *
     * @param player The player to check permissions for
     * @param permissionPrefix The prefix for cooldown permissions
     * @param defaultCooldown Default cooldown value if no specific permission is found
     * @return The cooldown value in seconds
     */
    private int getPlayerCooldown(Player player, String permissionPrefix, int defaultCooldown) {
        // Maximum cooldown value to check (for performance)
        final int MAX_COOLDOWN_CHECK = 300; // Check up to 300 seconds/5 minutes

        // Check for cooldown values from 0 up to MAX_COOLDOWN_CHECK
        for (int i = 0; i <= MAX_COOLDOWN_CHECK; i++) {
            if (player.hasPermission(permissionPrefix + i)) {
                return i;
            }
        }

        // Return default if no specific permission found
        return defaultCooldown;
    }


    /**
     * Helper method to check if a player is on cooldown
     */
    private boolean isOnCooldown(UUID playerUUID, Map<UUID, Long> cooldownMap, int cooldownSeconds) {
        if (!cooldownMap.containsKey(playerUUID)) {
            return false;
        }

        long lastUsed = cooldownMap.get(playerUUID);
        long cooldownMillis = cooldownSeconds * 1000L;

        return System.currentTimeMillis() - lastUsed < cooldownMillis;
    }

    /**
     * Helper method to get remaining cooldown time in seconds
     */
    private int getRemainingCooldown(UUID playerUUID, Map<UUID, Long> cooldownMap, int cooldownSeconds) {
        if (!cooldownMap.containsKey(playerUUID)) {
            return 0;
        }

        long lastUsed = cooldownMap.get(playerUUID);
        long cooldownMillis = cooldownSeconds * 1000L;
        long elapsedMillis = System.currentTimeMillis() - lastUsed;

        if (elapsedMillis >= cooldownMillis) {
            return 0;
        }

        return (int) Math.ceil((cooldownMillis - elapsedMillis) / 1000.0);
    }
}
