package me.kermx.prismaUtils.integrations;

import dev.geco.gsit.api.GSitAPI;
import dev.geco.gsit.object.GSeat;
import dev.geco.gsit.object.GStopReason;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Logger;

/**
 * Service class for integrating with GSit functionality
 * Allows the plugin to force players to sit or stand up
 */
public class SitService {

    private final Logger logger;
    private boolean gsitAvailable;
    private Plugin gsitPlugin;

    public SitService(PluginManager pluginManager, Logger logger) {
        this.logger = logger;
        registerGSit(pluginManager);
    }

    private void registerGSit(PluginManager pm) {
        gsitPlugin = pm.getPlugin("GSit");
        if (gsitPlugin != null && gsitPlugin.isEnabled()) {
            gsitAvailable = true;
            logger.info("GSit integration enabled");
        }
    }

    /**
     * Checks if GSit is available on this server
     * @return true if GSit is available, false otherwise
     */
    public boolean isGSitAvailable() {
        return gsitAvailable;
    }

    /**
     * Checks if a player is currently sitting
     * @param player The player to check
     * @return true if the player is sitting, false otherwise
     */
    public boolean isPlayerSitting(Player player) {
        if (!gsitAvailable) {
            return false;
        }

        try {
            return GSitAPI.isEntitySitting(player);
        } catch (Exception e) {
            logger.warning("Error checking if player is sitting: " + e.getMessage());
            return false;
        }
    }

    /**
     * Force a player to sit on a specific block
     * @param player The player to sit
     * @param block The block to sit on
     * @return true if the player was seated successfully, false otherwise
     */
    public boolean sitPlayer(Player player, Block block) {
        if (!gsitAvailable) {
            return false;
        }

        try {
            GSeat seat = GSitAPI.createSeat(block, player);
            return seat != null;
        } catch (Exception e) {
            logger.warning("Error making player sit: " + e.getMessage());
            return false;
        }
    }

    /**
     * Force a player to sit on a specific block with custom parameters
     * @param player The player to sit
     * @param block The block to sit on
     * @param canRotate Whether the player can rotate while sitting
     * @param seatRotation The rotation of the seat
     * @param centered Whether the player should be centered on the block
     * @return true if the player was seated successfully, false otherwise
     */
    public boolean sitPlayer(Player player, Block block, boolean canRotate, float seatRotation, boolean centered) {
        if (!gsitAvailable) {
            return false;
        }

        try {
            GSeat seat = GSitAPI.createSeat(block, player, canRotate, seatRotation, centered);
            return seat != null;
        } catch (Exception e) {
            logger.warning("Error making player sit with custom parameters: " + e.getMessage());
            return false;
        }
    }

    /**
     * Force a player to sit on a specific block with detailed positioning
     * @param player The player to sit
     * @param block The block to sit on
     * @param canRotate Whether the player can rotate while sitting
     * @param xOffset X offset for positioning
     * @param yOffset Y offset for positioning
     * @param zOffset Z offset for positioning
     * @param seatRotation The rotation of the seat
     * @param centered Whether the player should be centered on the block
     * @return true if the player was seated successfully, false otherwise
     */
    public boolean sitPlayer(Player player, Block block, boolean canRotate, double xOffset, double yOffset, double zOffset, float seatRotation, boolean centered) {
        if (!gsitAvailable) {
            return false;
        }

        try {
            GSeat seat = GSitAPI.createSeat(block, player, canRotate, xOffset, yOffset, zOffset, seatRotation, centered);
            return seat != null;
        } catch (Exception e) {
            logger.warning("Error making player sit with detailed positioning: " + e.getMessage());
            return false;
        }
    }

    /**
     * Force a player to stand up if they are sitting
     * @param player The player to make stand
     * @return true if the player was made to stand successfully, false otherwise
     */
    public boolean standPlayer(Player player) {
        if (!gsitAvailable) {
            return false;
        }

        try {
            // Check if player is sitting
            if (!GSitAPI.isEntitySitting(player)) {
                return false;
            }

            // Get the player's seat and remove it
            GSeat seat = GSitAPI.getSeatByEntity(player);
            if (seat != null) {
                return GSitAPI.removeSeat(seat, GStopReason.PLUGIN);
            }
            return false;
        } catch (Exception e) {
            logger.warning("Error making player stand: " + e.getMessage());
            return false;
        }
    }

    /**
     * Force a player to stand up with custom dismount options
     * @param player The player to make stand
     * @param useSafeDismount Whether to use safe dismount (teleport to safe location)
     * @return true if the player was made to stand successfully, false otherwise
     */
    public boolean standPlayer(Player player, boolean useSafeDismount) {
        if (!gsitAvailable) {
            return false;
        }

        try {
            // Check if player is sitting
            if (!GSitAPI.isEntitySitting(player)) {
                return false;
            }

            // Get the player's seat and remove it
            GSeat seat = GSitAPI.getSeatByEntity(player);
            if (seat != null) {
                return GSitAPI.removeSeat(seat, GStopReason.PLUGIN, useSafeDismount);
            }
            return false;
        } catch (Exception e) {
            logger.warning("Error making player stand with custom dismount: " + e.getMessage());
            return false;
        }
    }

    /**
     * Toggle sitting for a player
     * If standing, will sit on the block at their location
     * If sitting, will stand them up
     * @param player The player to toggle sitting for
     * @return true if the operation was successful, false otherwise
     */
    public boolean togglePlayerSitting(Player player) {
        if (!gsitAvailable) {
            return false;
        }

        try {
            if (GSitAPI.isEntitySitting(player)) {
                return standPlayer(player);
            } else {
                Block block = player.getLocation().getBlock();
                return sitPlayer(player, block);
            }
        } catch (Exception e) {
            logger.warning("Error toggling player sitting: " + e.getMessage());
            return false;
        }
    }

    /**
     * Set whether a player can use GSit's sit functionality
     * @param player The player to set the permission for
     * @param canSit Whether the player can sit
     */
    public void setPlayerCanSit(Player player, boolean canSit) {
        if (!gsitAvailable) {
            return;
        }

        try {
            GSitAPI.setEntityCanUseSit(player, canSit);
        } catch (Exception e) {
            logger.warning("Error setting player can sit permission: " + e.getMessage());
        }
    }

    /**
     * Check if a player can use GSit's sit functionality
     * @param player The player to check
     * @return true if the player can sit, false otherwise
     */
    public boolean canPlayerSit(Player player) {
        if (!gsitAvailable) {
            return false;
        }

        try {
            return GSitAPI.canEntityUseSit(player);
        } catch (Exception e) {
            logger.warning("Error checking if player can sit: " + e.getMessage());
            return false;
        }
    }
}

