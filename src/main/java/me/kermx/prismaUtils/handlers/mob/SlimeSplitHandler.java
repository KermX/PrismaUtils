package me.kermx.prismaUtils.handlers.mob;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class SlimeSplitHandler implements Listener {

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if ((entity.getType() == EntityType.SLIME || entity.getType() == EntityType.MAGMA_CUBE)
                && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            if (entity.getType() == EntityType.SLIME) {
                ((org.bukkit.entity.Slime) entity).setSize(1);
            } else if (entity.getType() == EntityType.MAGMA_CUBE) {
                ((org.bukkit.entity.MagmaCube) entity).setSize(1);
            }
        }
    }


    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.MAGMA_CUBE && ((MagmaCube) entity).getSize() == 1) {
            // Get the last damage cause
            EntityDamageEvent lastDamage = entity.getLastDamageCause();

            // Skip custom drop logic if killed by a frog (to preserve froglight drops)
            if (lastDamage instanceof EntityDamageByEntityEvent damageByEntityEvent &&
                    damageByEntityEvent.getDamager().getType() == EntityType.FROG) {
                return;
            }

            // Original custom drop logic
            if (Math.random() <= 0.25) {
                int quantity = (int) (Math.random() * 3) + 1;
                event.getDrops().add(new ItemStack(Material.MAGMA_CREAM, quantity));
            }
        }
    }
}

