package me.kermx.prismaUtils.handlers.mob;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ArmoredMountInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SaddledMountInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Rotten flesh zombifies mounts (horse/skeleton horse -> zombie horse, camel -> camel husk,
 * nautilus -> zombie nautilus). Zombified mounts can be cured zombie-villager style:
 * splash them with weakness, then feed a golden apple.
 * Shears still strip a zombie horse back to a skeleton horse.
 * <p>
 * The API only exposes coat colour/style on a regular {@link Horse} and the temperate/warm
 * variant on a {@link ZombieNautilus}, so those attributes are stashed in the mount's
 * persistent data on the way in and restored on the way out to survive a round trip.
 */
public class HorseZombificationHandler implements Listener {
    private static final double TRANSFORMATION_CHANCE = 0.1;

    // Zombie-villager style cure delay
    private static final int CURE_MIN_TICKS = 60;   // 3 seconds
    private static final int CURE_MAX_TICKS = 160;  // 8 seconds

    private final PrismaUtils plugin;
    private final Random random = new Random();
    private final Set<UUID> pendingCures = new HashSet<>();

    // Keys used to remember attributes the target type can't hold, so a round trip restores them
    private final NamespacedKey coatColorKey;
    private final NamespacedKey coatStyleKey;
    private final NamespacedKey nautilusVariantKey;

    public HorseZombificationHandler(PrismaUtils plugin) {
        this.plugin = plugin;
        this.coatColorKey = new NamespacedKey(plugin, "coat_color");
        this.coatStyleKey = new NamespacedKey(plugin, "coat_style");
        this.nautilusVariantKey = new NamespacedKey(plugin, "nautilus_variant");
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // Fires once per hand; only act on the main hand so we don't consume twice
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof LivingEntity target)) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        switch (itemInHand.getType()) {
            case ROTTEN_FLESH -> handleZombification(event, target, itemInHand);
            case GOLDEN_APPLE -> handleCure(event, target, itemInHand);
            case SHEARS -> handleShearing(event, target, itemInHand);
            default -> {
            }
        }
    }

    private void handleZombification(PlayerInteractEntityEvent event, LivingEntity target, ItemStack itemInHand) {
        Class<? extends LivingEntity> zombified = zombifiedForm(target);
        if (zombified == null) return;

        event.setCancelled(true);
        itemInHand.setAmount(itemInHand.getAmount() - 1);

        Location loc = target.getLocation();
        if (random.nextDouble() < TRANSFORMATION_CHANCE) {
            transform(target, zombified, zombifySetup(target));
            loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 10, 0.5, 0.5, 0.5);
            loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 1.0f);
        } else {
            loc.getWorld().spawnParticle(Particle.SMOKE, loc, 10, 0.5, 0.5, 0.5);
        }
    }

    private void handleCure(PlayerInteractEntityEvent event, LivingEntity target, ItemStack itemInHand) {
        Class<? extends LivingEntity> cured = curedForm(target);
        if (cured == null) return;

        // Same requirement as curing a zombie villager: the mob must be weakened first
        if (!target.hasPotionEffect(PotionEffectType.WEAKNESS)) return;

        event.setCancelled(true);

        // Already shaking - don't eat another golden apple
        if (!pendingCures.add(target.getUniqueId())) return;

        itemInHand.setAmount(itemInHand.getAmount() - 1);
        target.removePotionEffect(PotionEffectType.WEAKNESS);

        Location loc = target.getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);

        // Capture the carry-over now, while the zombified mob is definitely still valid
        startCureCountdown(target, cured, cureSetup(target));
    }

    /**
     * Shakes the mob with angry villager particles for a few seconds, then converts it.
     */
    private void startCureCountdown(LivingEntity target, Class<? extends LivingEntity> cured, Consumer<LivingEntity> extraSetup) {
        UUID uuid = target.getUniqueId();
        int cureTicks = CURE_MIN_TICKS + random.nextInt(CURE_MAX_TICKS - CURE_MIN_TICKS + 1);

        new BukkitRunnable() {
            private int ticksElapsed = 0;

            @Override
            public void run() {
                // Mob died, despawned, or its chunk unloaded before the cure finished
                if (!target.isValid()) {
                    pendingCures.remove(uuid);
                    cancel();
                    return;
                }

                if (ticksElapsed >= cureTicks) {
                    pendingCures.remove(uuid);
                    cancel();

                    Location loc = target.getLocation();
                    transform(target, cured, extraSetup);
                    loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 20, 0.5, 0.5, 0.5);
                    loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.0f);
                    return;
                }

                target.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, target.getLocation(), 3, 0.4, 0.6, 0.4);
                ticksElapsed += 10;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void handleShearing(PlayerInteractEntityEvent event, LivingEntity target, ItemStack itemInHand) {
        if (!(target instanceof ZombieHorse)) return;

        event.setCancelled(true);

        Location loc = target.getLocation();
        transform(target, SkeletonHorse.class, null);
        ItemUtils.damageItem(itemInHand, 1);

        loc.getWorld().spawnParticle(Particle.CRIT, loc, 10, 0.5, 0.5, 0.5);
    }

    /**
     * The zombified counterpart of a mob, or null if it has none / is already zombified.
     */
    private Class<? extends LivingEntity> zombifiedForm(LivingEntity entity) {
        // CamelHusk extends Camel, so it has to be ruled out first
        if (entity instanceof CamelHusk) return null;
        // ZombieHorse/SkeletonHorse are not instanceof Horse, so this only matches regular horses
        if (entity instanceof Horse || entity instanceof SkeletonHorse) return ZombieHorse.class;
        if (entity instanceof Camel) return CamelHusk.class;
        if (entity instanceof Nautilus) return ZombieNautilus.class;
        return null;
    }

    /**
     * The cured counterpart of a zombified mob, or null if it isn't curable.
     */
    private Class<? extends LivingEntity> curedForm(LivingEntity entity) {
        if (entity instanceof ZombieHorse) return Horse.class;
        if (entity instanceof CamelHusk) return Camel.class;
        if (entity instanceof ZombieNautilus) return Nautilus.class;
        return null;
    }

    /**
     * Builds the extra setup applied to the zombified result: stash a regular horse's coat
     * so a later cure restores it, and re-apply a nautilus variant remembered from a prior cure.
     */
    private Consumer<LivingEntity> zombifySetup(LivingEntity source) {
        if (source instanceof Horse horse) {
            String colorName = horse.getColor().name();
            String styleName = horse.getStyle().name();
            return spawned -> {
                PersistentDataContainer pdc = spawned.getPersistentDataContainer();
                pdc.set(coatColorKey, PersistentDataType.STRING, colorName);
                pdc.set(coatStyleKey, PersistentDataType.STRING, styleName);
            };
        }
        if (source instanceof Nautilus) {
            // Plain nautilus exposes no variant, but a prior cure may have stashed one
            String variantKey = source.getPersistentDataContainer().get(nautilusVariantKey, PersistentDataType.STRING);
            if (variantKey != null) {
                ZombieNautilus.Variant variant = lookupNautilusVariant(variantKey);
                if (variant != null) {
                    return spawned -> {
                        if (spawned instanceof ZombieNautilus zombieNautilus) {
                            zombieNautilus.setVariant(variant);
                        }
                    };
                }
            }
        }
        return null;
    }

    /**
     * Builds the extra setup applied to the cured result: restore a zombie horse's remembered
     * coat, and stash a zombie nautilus's variant onto the plain nautilus for a future re-zombify.
     */
    private Consumer<LivingEntity> cureSetup(LivingEntity source) {
        if (source instanceof ZombieHorse) {
            PersistentDataContainer pdc = source.getPersistentDataContainer();
            String colorName = pdc.get(coatColorKey, PersistentDataType.STRING);
            String styleName = pdc.get(coatStyleKey, PersistentDataType.STRING);
            Horse.Color color = enumValue(Horse.Color.class, colorName);
            Horse.Style style = enumValue(Horse.Style.class, styleName);
            if (color == null && style == null) {
                return null; // No remembered coat -> let the game pick one (random, vanilla-style)
            }
            return spawned -> {
                if (spawned instanceof Horse horse) {
                    if (color != null) horse.setColor(color);
                    if (style != null) horse.setStyle(style);
                }
            };
        }
        if (source instanceof ZombieNautilus zombieNautilus) {
            String variantKey = zombieNautilus.getVariant().getKey().asString();
            return spawned -> spawned.getPersistentDataContainer()
                    .set(nautilusVariantKey, PersistentDataType.STRING, variantKey);
        }
        return null;
    }

    private ZombieNautilus.Variant lookupNautilusVariant(String key) {
        NamespacedKey namespacedKey = NamespacedKey.fromString(key);
        if (namespacedKey == null) return null;
        Registry<ZombieNautilus.Variant> registry =
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ZOMBIE_NAUTILUS_VARIANT);
        return registry.get(namespacedKey);
    }

    private <E extends Enum<E>> E enumValue(Class<E> type, String name) {
        if (name == null) return null;
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Replaces a mob with its zombified/cured counterpart, carrying over as much
     * state as the two types have in common. {@code extraSetup} runs last, for
     * type-specific attributes (coat, variant) the common path can't handle.
     */
    private void transform(LivingEntity source, Class<? extends LivingEntity> targetType, Consumer<LivingEntity> extraSetup) {
        Location loc = source.getLocation();

        double health = source.getHealth();
        Double maxHealth = attributeValue(source, Attribute.MAX_HEALTH);
        Double speed = attributeValue(source, Attribute.MOVEMENT_SPEED);
        Double jumpStrength = attributeValue(source, Attribute.JUMP_STRENGTH);

        Component customName = source.customName();
        boolean customNameVisible = source.isCustomNameVisible();

        AnimalTamer owner = source instanceof Tameable tameable ? tameable.getOwner() : null;
        int age = source instanceof Ageable ageable ? ageable.getAge() : 0;
        boolean sitting = source instanceof Sittable sittable && sittable.isSitting();

        ItemStack saddle = null;
        ItemStack armor = null;
        if (source instanceof InventoryHolder holder) {
            if (holder.getInventory() instanceof SaddledMountInventory saddled) {
                saddle = saddled.getSaddle();
            }
            if (holder.getInventory() instanceof ArmoredMountInventory armored) {
                armor = armored.getArmor();
            }
        }

        source.remove();

        ItemStack finalSaddle = saddle;
        ItemStack finalArmor = armor;
        loc.getWorld().spawn(loc, targetType, spawned -> {
            applyAttribute(spawned, Attribute.MAX_HEALTH, maxHealth);
            applyAttribute(spawned, Attribute.MOVEMENT_SPEED, speed);
            applyAttribute(spawned, Attribute.JUMP_STRENGTH, jumpStrength);

            // Clamp: the new type's max health may be lower than the old one's current health
            Double newMaxHealth = attributeValue(spawned, Attribute.MAX_HEALTH);
            spawned.setHealth(newMaxHealth != null ? Math.min(health, newMaxHealth) : health);

            spawned.customName(customName);
            spawned.setCustomNameVisible(customNameVisible);

            if (spawned instanceof Tameable tameable) {
                // Matches the previous behaviour: a transformed mount always comes out tamed
                tameable.setTamed(true);
                if (owner != null) {
                    tameable.setOwner(owner);
                }
            }
            if (spawned instanceof Ageable ageable) {
                ageable.setAge(age);
            }
            if (spawned instanceof Sittable sittable) {
                sittable.setSitting(sitting);
            }

            if (spawned instanceof InventoryHolder holder) {
                if (finalSaddle != null && holder.getInventory() instanceof SaddledMountInventory saddled) {
                    saddled.setSaddle(finalSaddle);
                }
                if (finalArmor != null && holder.getInventory() instanceof ArmoredMountInventory armored) {
                    armored.setArmor(finalArmor);
                }
            }

            if (extraSetup != null) {
                extraSetup.accept(spawned);
            }
        });
    }

    private Double attributeValue(LivingEntity entity, Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance != null ? instance.getValue() : null;
    }

    private void applyAttribute(LivingEntity entity, Attribute attribute, Double value) {
        if (value == null) return;
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }
}
