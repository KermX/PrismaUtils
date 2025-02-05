package me.kermx.prismaUtils.handlers;

import me.kermx.prismaUtils.managers.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class SilkSpawnerHandler implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // Check if the block broken is a spawner
        if (block.getType() == Material.SPAWNER) {
            ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();

            boolean hasSilkTouch = isSilkTouch(tool);
            boolean isSneaking = event.getPlayer().isSneaking();

            // If the player does NOT have Silk Touch...
            if (!hasSilkTouch) {
                // ...but is also NOT sneaking, cancel the break to prevent accidental spawner loss
                if (!isSneaking) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(
                            MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().spawnerNoSilkWarningMessage));
                }
                // If they are sneaking but don't have Silk Touch, the event proceeds normally,
                // meaning the spawner is destroyed (no special drop).
                return;
            }

            // If the player has Silk Touch, handle dropping the spawner with the correct mob type
            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            EntityType spawnedType = spawner.getSpawnedType();

            // Prevent default block drops / XP
            event.setExpToDrop(0);

            // Create the Silk Touched spawner item
            ItemStack spawnerItem = createSpawnerItem(spawnedType);

            // Drop it naturally
            block.getWorld().dropItemNaturally(block.getLocation(), spawnerItem);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Check if the placed item is a spawner
        ItemStack item = event.getItemInHand();
        if (item.getType() == Material.SPAWNER && item.hasItemMeta()) {
            if (item.getItemMeta() instanceof BlockStateMeta meta) {
                if (meta.getBlockState() instanceof CreatureSpawner spawnerMeta) {
                    // Retrieve the spawner's EntityType stored in the item
                    EntityType mob = spawnerMeta.getSpawnedType();

                    // Apply the mob type to the newly placed spawner
                    Block block = event.getBlockPlaced();
                    BlockState blockState = block.getState();
                    if (blockState instanceof CreatureSpawner newSpawner) {
                        newSpawner.setSpawnedType(mob);
                        newSpawner.update(true, false);
                    }
                }
            }
        }
    }

    /**
     * Utility method to check if an ItemStack has Silk Touch.
     */
    private boolean isSilkTouch(ItemStack tool) {
        if (tool == null) return false;
        // Optional: Restrict to pickaxes only to mimic vanilla spawner mining
        if (!Tag.ITEMS_PICKAXES.isTagged(tool.getType())) return false;

        // Check Silk Touch enchant
        return tool.containsEnchantment(Enchantment.SILK_TOUCH);
    }

    /**
     * Create an ItemStack of a spawner that retains the EntityType.
     */
    private ItemStack createSpawnerItem(EntityType spawnedType) {
        ItemStack spawnerItem = new ItemStack(Material.SPAWNER, 1);

        if (spawnerItem.getItemMeta() instanceof BlockStateMeta blockStateMeta) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();

            creatureSpawner.setSpawnedType(spawnedType);
            blockStateMeta.setBlockState(creatureSpawner);

            // Optional: set a custom display name
            blockStateMeta.displayName(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().spawnerName,
                    Placeholder.component("entitytype", Component.translatable(spawnedType.translationKey()))));

            spawnerItem.setItemMeta(blockStateMeta);
        }
        return spawnerItem;
    }
}