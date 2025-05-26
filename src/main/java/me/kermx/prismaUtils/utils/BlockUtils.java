package me.kermx.prismaUtils.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Objects;

public final class BlockUtils {

    private BlockUtils() {
        throw new UnsupportedOperationException("Utility class (BlockUtils) - cannot be instantiated");
    }


    /**
     * Checks if a block is protected by firing a BlockBreakEvent.
     * <p>
     * Note: This method fires an event, which may have side effects
     * such as triggering other plugins' event handlers.
     * Note: This method should only be used for blocks that likely will not
     * affect other plugins' event handlers.
     *
     * @param player The player attempting to break the block.
     * @param block  The block to be checked.
     * @return True if the block break event is cancelled, false otherwise.
     */
    public static boolean blockIsProtected(Player player, Block block) {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(block, "block cannot be null");

        BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(breakEvent);
        boolean cancelledByProtection = breakEvent.isCancelled();
        breakEvent.setCancelled(true);
        return cancelledByProtection;
    }

    /**
     * Retrieves the blocks adjacent to the given block in the six cardinal directions.
     *
     * @param block The center block.
     * @return An array of the six adjacent blocks.
     */
    public static Block[] getAdjacentBlocks(Block block) {
        Objects.requireNonNull(block, "block cannot be null");
        return new Block[]{
                block.getRelative(BlockFace.NORTH),
                block.getRelative(BlockFace.SOUTH),
                block.getRelative(BlockFace.EAST),
                block.getRelative(BlockFace.WEST),
                block.getRelative(BlockFace.UP),
                block.getRelative(BlockFace.DOWN)
        };
    }

    /**
     * Generates a random location within the specified radius of a center location.
     * The randomized location will maintain the same yaw and pitch as the center location.
     *
     * @param center The center location
     * @param radius The maximum distance from the center
     * @return A new randomized location
     */
    public static Location getRandomLocationNear(Location center, double radius) {
        if (radius <= 0) {
            return center.clone(); // No randomization if radius is 0 or negative
        }

        // Generate random angle and distance
        double angle = Math.random() * 2 * Math.PI; // Random angle in radians (0 to 2π)
        double distance = Math.random() * radius;   // Random distance up to the max radius

        // Convert polar coordinates to Cartesian coordinates
        double offsetX = distance * Math.cos(angle);
        double offsetZ = distance * Math.sin(angle);

        // Create a new location with the same world, yaw, and pitch
        Location newLocation = center.clone();
        newLocation.add(offsetX, 0, offsetZ);

        return newLocation;
    }

    public static Inventory getInventoryFromBlock(Block block) {
        Objects.requireNonNull(block, "block cannot be null");
        return block.getState() instanceof InventoryHolder ? ((InventoryHolder) block.getState()).getInventory() : null;
    }
}
