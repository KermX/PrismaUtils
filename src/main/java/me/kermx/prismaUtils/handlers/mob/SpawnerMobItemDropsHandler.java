package me.kermx.prismaUtils.handlers.mob;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class SpawnerMobItemDropsHandler implements Listener {

    private final Plugin plugin;
    private final NamespacedKey fromSpawnerKey;
    private final NamespacedKey damagedByPlayerKey;

    public SpawnerMobItemDropsHandler(Plugin plugin) {
        this.plugin = plugin;
        this.fromSpawnerKey = new NamespacedKey(plugin, "from_spawner");
        this.damagedByPlayerKey = new NamespacedKey(plugin, "damaged_by_player");
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            LivingEntity entity = event.getEntity();
            entity.getPersistentDataContainer().set(
                    fromSpawnerKey,
                    PersistentDataType.BYTE,
                    (byte) 1
            );
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        PersistentDataContainer data = entity.getPersistentDataContainer();
        if (!data.has(fromSpawnerKey, PersistentDataType.BYTE)) {
            return;
        }

        // Check damager
        if (event.getDamager() instanceof Player) {
            data.set(damagedByPlayerKey, PersistentDataType.BYTE, (byte) 1);
        } else if (event.getDamager() instanceof Projectile projectile &&
                projectile.getShooter() instanceof Player) {
            data.set(damagedByPlayerKey, PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        PersistentDataContainer data = entity.getPersistentDataContainer();

        if (data.has(fromSpawnerKey, PersistentDataType.BYTE)) {
            if (shouldPreserveSpecialDrops(entity)){
                return;
            }
            if (!data.has(damagedByPlayerKey, PersistentDataType.BYTE)) {
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
        }
    }

    private boolean shouldPreserveSpecialDrops(LivingEntity entity){
        EntityDamageEvent lastDamage = entity.getLastDamageCause();

        if (lastDamage instanceof EntityDamageByEntityEvent damageByEntityEvent){
            if (entity.getType() == EntityType.MAGMA_CUBE &&
                    damageByEntityEvent.getDamager().getType() == EntityType.FROG) {
                return true;
            }
            if (entity.getType() == EntityType.CREEPER &&
                    damageByEntityEvent.getDamager() instanceof Projectile projectile &&
                    projectile.getShooter() instanceof LivingEntity shooter &&
                    shooter.getType() == EntityType.SKELETON) {
                return true;
            }
            if (lastDamage.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION &&
                    damageByEntityEvent.getDamager().getType() == EntityType.CREEPER) {
                Creeper creeper = (Creeper) damageByEntityEvent.getDamager();
                if (creeper.isPowered()) {
                    return true;
                }
            }
//            if (damageByEntityEvent.getDamager().getType() == EntityType.WITHER ||
//                    (damageByEntityEvent.getDamager() instanceof WitherSkull)) {
//                return true;
//            }

        }
        return false;
    }
}