package me.kermx.prismaUtils.handlers.mob;

import me.kermx.prismaUtils.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class HorseZombificationHandler implements Listener {
    private static final double TRANSFORMATION_CHANCE = 0.1;
    private final Random random = new Random();

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity clickedEntity = event.getRightClicked();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (clickedEntity instanceof SkeletonHorse &&
                itemInHand.getType() == Material.ROTTEN_FLESH) {

            event.setCancelled(true);

            int amount = itemInHand.getAmount();
            itemInHand.setAmount(amount - 1);

            SkeletonHorse skeletonHorse = (SkeletonHorse) clickedEntity;
            Location loc = skeletonHorse.getLocation();

            if (random.nextDouble() < TRANSFORMATION_CHANCE) {

                double health = skeletonHorse.getHealth();
                double maxHealth = skeletonHorse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double speed = skeletonHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
                double jumpHeight = skeletonHorse.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).getValue();
                boolean hasSaddle = skeletonHorse.getInventory().getSaddle() != null;
                Component customName = skeletonHorse.customName();
                boolean customNameVisible = skeletonHorse.isCustomNameVisible();

                skeletonHorse.remove();

                ZombieHorse zombieHorse = (ZombieHorse) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE_HORSE);
                zombieHorse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
                zombieHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
                zombieHorse.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).setBaseValue(jumpHeight);
                zombieHorse.setHealth(health);
                zombieHorse.setTamed(true);
                if (hasSaddle) {
                    zombieHorse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                }
                zombieHorse.customName(customName);
                zombieHorse.setCustomNameVisible(customNameVisible);

                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 10, 0.5, 0.5, 0.5);
                } else {
                    loc.getWorld().spawnParticle(Particle.SMOKE, loc, 10, 0.5, 0.5, 0.5);
            }

            } else if (clickedEntity instanceof ZombieHorse zombieHorse &&
                itemInHand.getType() == Material.SHEARS) {

            event.setCancelled(true);

            Location loc = zombieHorse.getLocation();
            double health = zombieHorse.getHealth();
            double maxHealth = zombieHorse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double speed = zombieHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
            double jumpHeight = zombieHorse.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).getValue();
            boolean hasSaddle = zombieHorse.getInventory().getSaddle() != null;
            Component customName = zombieHorse.customName();
            boolean customNameVisible = zombieHorse.isCustomNameVisible();

            zombieHorse.remove();

            SkeletonHorse skeletonHorse = (SkeletonHorse) loc.getWorld().spawnEntity(loc, EntityType.SKELETON_HORSE);
            skeletonHorse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            skeletonHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
            skeletonHorse.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).setBaseValue(jumpHeight);
            skeletonHorse.setHealth(health);
            skeletonHorse.setTamed(true);
            if (hasSaddle) {
                skeletonHorse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            }
            skeletonHorse.customName(customName);
            skeletonHorse.setCustomNameVisible(customNameVisible);

            ItemUtils.damageItem(itemInHand, 1);

            loc.getWorld().spawnParticle(Particle.CRIT, loc, 10, 0.5, 0.5, 0.5);
        }
    }
}
