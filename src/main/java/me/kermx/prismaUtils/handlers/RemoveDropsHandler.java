package me.kermx.prismaUtils.handlers;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RemoveDropsHandler implements Listener {

    @EventHandler
    public void onRelevantEntityDeath(EntityDeathEvent event){
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.WITHER_SKELETON){
            List<ItemStack> drops = event.getDrops();

            drops.removeIf(drop -> drop.getType() == Material.STONE_SWORD);
        }

        if (entity.getType() == EntityType.SKELETON || entity.getType() == EntityType.STRAY){
            List<ItemStack> drops = event.getDrops();

            drops.removeIf(drop -> drop.getType() == Material.BOW);
        }

        if (entity.getType() == EntityType.ZOMBIFIED_PIGLIN || entity.getType() == EntityType.PIGLIN){
            List<ItemStack> drops = event.getDrops();

            drops.removeIf(drop -> drop.getType() == Material.GOLDEN_SWORD);
        }
    }
}
