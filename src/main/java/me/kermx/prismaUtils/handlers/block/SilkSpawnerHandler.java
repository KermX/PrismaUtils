package me.kermx.prismaUtils.handlers.block;

import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.ItemUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
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

        if (block.getType() == Material.SPAWNER) {
            ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();

            boolean isSneaking = event.getPlayer().isSneaking();

            if (!ItemUtils.itemHasEnchantments(tool, Enchantment.SILK_TOUCH) && ItemUtils.itemIsTagged(tool, Tag.ITEMS_PICKAXES)) {
                if (!isSneaking) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(
                            TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().spawnerNoSilkWarningMessage));
                }
                return;
            }

            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            EntityType spawnedType = spawner.getSpawnedType();
            event.setExpToDrop(0);
            ItemStack spawnerItem = createSpawnerItem(spawnedType);
            block.getWorld().dropItemNaturally(block.getLocation(), spawnerItem);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() == Material.SPAWNER && item.hasItemMeta()) {
            if (item.getItemMeta() instanceof BlockStateMeta meta) {
                if (meta.getBlockState() instanceof CreatureSpawner spawnerMeta) {

                    EntityType mob = spawnerMeta.getSpawnedType();

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
     * Create an ItemStack of a spawner that retains the EntityType.
     */
    private ItemStack createSpawnerItem(EntityType spawnedType) {
        ItemStack spawnerItem = new ItemStack(Material.SPAWNER, 1);

        if (spawnerItem.getItemMeta() instanceof BlockStateMeta blockStateMeta) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();

            creatureSpawner.setSpawnedType(spawnedType);
            blockStateMeta.setBlockState(creatureSpawner);

            blockStateMeta.displayName(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().spawnerName,
                            Placeholder.component("entitytype", Component.translatable(spawnedType.translationKey())))
            );

            spawnerItem.setItemMeta(blockStateMeta);
        }
        return spawnerItem;
    }
}