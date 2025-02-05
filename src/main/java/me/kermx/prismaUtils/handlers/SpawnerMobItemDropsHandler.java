package me.kermx.prismaUtils.handlers;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        PersistentDataContainer data = entity.getPersistentDataContainer();

        if (data.has(fromSpawnerKey, PersistentDataType.BYTE)) {
            if (!data.has(damagedByPlayerKey, PersistentDataType.BYTE)) {
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
        }
    }
}