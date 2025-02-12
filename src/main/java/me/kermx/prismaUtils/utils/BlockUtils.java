package me.kermx.prismaUtils.utils;

import org.bukkit.Bukkit;
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
     *
     * @param player The player attempting to break the block.
     * @param block  The block to be checked.
     * @return True if the block break event is cancelled, false otherwise.
     */
    // Need to re-evaluate this method, may lead to other plugins acting on the event before we cancel it.
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
    public static Block[] getAdjacentBlocks(Block block){
        Objects.requireNonNull(block, "block cannot be null");
        return new Block[] {
                block.getRelative(BlockFace.NORTH),
                block.getRelative(BlockFace.SOUTH),
                block.getRelative(BlockFace.EAST),
                block.getRelative(BlockFace.WEST),
                block.getRelative(BlockFace.UP),
                block.getRelative(BlockFace.DOWN)
        };
    }

    public static Inventory getInventoryFromBlock(Block block) {
        Objects.requireNonNull(block, "block cannot be null");
        return block.getState() instanceof InventoryHolder ? ((InventoryHolder) block.getState()).getInventory() : null;
    }
}
