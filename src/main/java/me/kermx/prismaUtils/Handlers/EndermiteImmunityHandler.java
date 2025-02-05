package me.kermx.prismaUtils.Handlers;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EndermiteImmunityHandler implements Listener {

    @EventHandler
    public void onEndermiteDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.ENDERMITE
                && event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            event.setCancelled(true);
        }
    }
}
