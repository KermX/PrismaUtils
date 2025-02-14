package me.kermx.prismaUtils.handlers.mob;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.PiglinAbstract;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class NetherMobZombificationHandler implements Listener {

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        EntityType spawnedEntity = event.getEntityType();
        World.Environment worldEnvironment = event.getLocation().getWorld().getEnvironment();
        if (worldEnvironment == World.Environment.NORMAL) {
            if (spawnedEntity == EntityType.PIGLIN) {
                PiglinAbstract piglin = (PiglinAbstract) event.getEntity();
                piglin.setImmuneToZombification(true);
            }
            if (spawnedEntity == EntityType.HOGLIN) {
                Hoglin hoglin = (Hoglin) event.getEntity();
                hoglin.setImmuneToZombification(true);
            }
            if (spawnedEntity == EntityType.PIGLIN_BRUTE) {
                PiglinBrute piglinBrute = (PiglinBrute) event.getEntity();
                piglinBrute.setImmuneToZombification(true);
            }
        }
    }

    @EventHandler
    public void onPortalTeleport(EntityPortalEvent event) {
        EntityType portaledEntity = event.getEntityType();
        if (portaledEntity == EntityType.PIGLIN) {
            PiglinAbstract piglin = (PiglinAbstract) event.getEntity();
            piglin.setImmuneToZombification(true);
        }
        if (portaledEntity == EntityType.HOGLIN) {
            Hoglin hoglin = (Hoglin) event.getEntity();
            hoglin.setImmuneToZombification(true);
        }
        if (portaledEntity == EntityType.PIGLIN_BRUTE) {
            PiglinBrute piglinBrute = (PiglinBrute) event.getEntity();
            piglinBrute.setImmuneToZombification(true);
        }
    }
}
