package me.kermx.prismaUtils.handlers.mob;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HappyGhast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Makes the Speed potion effect boost a happy ghast's flight. The vanilla Speed effect only
 * modifies MOVEMENT_SPEED, which doesn't govern happy ghast flight, so this mirrors the effect
 * onto the FLYING_SPEED attribute instead.
 * <p>
 * The boost is a transient attribute modifier (never written to the entity's NBT), so nothing can
 * be left orphaned on the entity. It's kept in sync purely by events:
 * <ul>
 *   <li>{@link EntityPotionEffectEvent} covers the effect being added, changed, cleared, or
 *       removed — including natural expiry, which fires as a REMOVED action with an EXPIRATION
 *       cause.</li>
 *   <li>{@link EntitiesLoadEvent} re-applies the boost when a ghast with a still-active effect
 *       loads from disk, since the transient modifier didn't survive the unload and no potion
 *       event fires on load.</li>
 * </ul>
 */
public class HappyGhastSpeedHandler implements Listener {

    private final PrismaUtils plugin;
    private final double boostPerLevel;
    private final NamespacedKey modifierKey;

    public HappyGhastSpeedHandler(PrismaUtils plugin, double boostPerLevel) {
        this.plugin = plugin;
        this.boostPerLevel = boostPerLevel;
        this.modifierKey = new NamespacedKey(plugin, "happy_ghast_speed");
    }

    /**
     * Syncs the boost whenever a happy ghast's speed effect changes. The effect state isn't
     * settled until after this event (e.g. a REMOVED effect is still present right now), so the
     * actual sync runs on the next tick.
     */
    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (!PotionEffectType.SPEED.equals(event.getModifiedType())) return;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (ghast.isValid()) {
                syncGhast(ghast);
            }
        });
    }

    /**
     * Re-applies the boost to any ghast that loads with a still-active speed effect; its transient
     * modifier was dropped on unload and no potion event fires on load.
     */
    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof HappyGhast ghast) {
                syncGhast(ghast);
            }
        }
    }

    /**
     * Ensures the ghast's FLYING_SPEED modifier matches its current speed effect: present and
     * correctly scaled while the effect is active, absent otherwise.
     */
    private void syncGhast(HappyGhast ghast) {
        AttributeInstance flyingSpeed = ghast.getAttribute(Attribute.FLYING_SPEED);
        if (flyingSpeed == null) return;

        PotionEffect speed = ghast.getPotionEffect(PotionEffectType.SPEED);
        AttributeModifier existing = flyingSpeed.getModifier(modifierKey);

        if (speed == null) {
            if (existing != null) {
                flyingSpeed.removeModifier(modifierKey);
            }
            return;
        }

        double desired = boostPerLevel * (speed.getAmplifier() + 1);
        if (existing != null) {
            if (existing.getAmount() == desired) {
                return; // already correct
            }
            flyingSpeed.removeModifier(modifierKey);
        }

        flyingSpeed.addTransientModifier(
                new AttributeModifier(modifierKey, desired, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
    }
}
