package me.kermx.prismaUtils.handlers.block;

import io.papermc.paper.event.entity.EntityInsideBlockEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class CauldronTransformHandler implements Listener {

    private static final double WATER_LEVEL_CONSUME_CHANCE = 0.05;

    @EventHandler
    public void onEntityInsideBlock(EntityInsideBlockEvent event) {
        if (!(event.getEntity() instanceof Item itemEntity)) {
            return;
        }

        Block cauldron = event.getBlock();
        if (cauldron.getType() != Material.WATER_CAULDRON) {
            return;
        }

        ItemStack stack = itemEntity.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        Material resultType = getTransformResult(stack.getType());
        if (resultType == null) {
            return;
        }

        ItemStack newStack = stack.withType(resultType);
        itemEntity.setItemStack(newStack);

        if (ThreadLocalRandom.current().nextDouble() < WATER_LEVEL_CONSUME_CHANCE) {
            if (cauldron.getBlockData() instanceof Levelled levelled) {
                int level = levelled.getLevel();
                if (level <= 1) {
                    cauldron.setType(Material.CAULDRON, false);
                } else {
                    levelled.setLevel(level - 1);
                    cauldron.setBlockData(levelled, false);
                }
            }
        }
    }

    private static Material getTransformResult(Material input) {
        if (input == Material.DIRT) {
            return Material.MUD;
        }

        String name = input.name();
        if (name.endsWith("_CONCRETE_POWDER")) {
            String concreteName = name.substring(0, name.length() - "_POWDER".length());
            try {
                return Material.valueOf(concreteName);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        return null;
    }
}
