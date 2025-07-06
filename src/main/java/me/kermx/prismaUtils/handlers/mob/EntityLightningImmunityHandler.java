package me.kermx.prismaUtils.handlers.mob;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityLightningImmunityHandler implements Listener {

    private final List<EntityType> immuneEntities = Arrays.asList(
            EntityType.ENDERMITE,
            EntityType.MINECART,
            EntityType.ARMOR_STAND,
            EntityType.ITEM_FRAME,
            EntityType.GLOW_ITEM_FRAME);

    @EventHandler
    public void onEndermiteDamage(EntityDamageEvent event) {
        if (immuneEntities.contains(event.getEntity().getType())
                && (event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK)) {

            event.setCancelled(true);
            event.getEntity().setFireTicks(0);
        }
    }
}
