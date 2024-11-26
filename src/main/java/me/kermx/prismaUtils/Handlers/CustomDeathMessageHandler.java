package me.kermx.prismaUtils.Handlers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.UUID;

public class CustomDeathMessageHandler implements Listener {


    private final ConfigUtils configUtils;
    private final HashMap<UUID, Long> playerDeathTimes = new HashMap<>();

    public CustomDeathMessageHandler(ConfigUtils configUtils){
        this.configUtils = configUtils;
    }

@EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event){
        Player deceased = event.getEntity();

        long currentTime = System.currentTimeMillis();
        Long lastDeathTime = playerDeathTimes.get(deceased.getUniqueId());
        long cooldownPeriod = configUtils.cooldownDeathMessageSeconds * 1000;

        if (lastDeathTime != null && (currentTime - lastDeathTime) < cooldownPeriod){
            event.deathMessage(null);
        } else {
            playerDeathTimes.put(deceased.getUniqueId(), currentTime);

            EntityDamageEvent lastDamageEvent = deceased.getLastDamageCause();
            Entity killer = null;

            if (lastDamageEvent instanceof EntityDamageByEntityEvent damageByEntityEvent){
                killer = damageByEntityEvent.getDamager();
            }

            Component deathMessage = constructDeathMessage(event, deceased, killer);

            event.deathMessage(deathMessage);
        }
    }

    private Component constructDeathMessage(PlayerDeathEvent event, Player deceased, Entity killer){
        Component message = null;

        if (killer != null){
            if (killer instanceof Player){
                message = handlePlayerKill(deceased, (Player) killer);
            } else if (killer instanceof Projectile) {
                message = handleProjectileKill(deceased, (Projectile) killer);
            } else {
                message = handleEntityKill(deceased, killer);
            }
        } else {
            message = handleOtherCause(deceased);
        }
        return message;
    }

    private Component handlePlayerKill(Player deceased, Player killer){
        String configMessage = configUtils.deathMessageKilledByPlayer;
        configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

        return MiniMessage.miniMessage().deserialize(configMessage,
                Placeholder.component("deceased", deceased.displayName()),
                Placeholder.component("killer", killer.displayName()),
                Placeholder.component("weapon", killer.getInventory().getItemInMainHand().displayName()));
    }

    private Component handleProjectileKill(Player deceased, Projectile projectile){
        ProjectileSource shooter = projectile.getShooter();

        if (shooter instanceof Player killer){

            String configMessage = configUtils.deathMessageShotByPlayer;
            configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

            return MiniMessage.miniMessage().deserialize(configMessage,
                    Placeholder.component("deceased", deceased.displayName()),
                    Placeholder.component("killer", killer.displayName()),
                    Placeholder.component("weapon", killer.getInventory().getItemInMainHand().displayName()));

        } else if (shooter instanceof Entity){
            Entity killer = (Entity) shooter;
            String configMessage = configUtils.deathMessageShotByEntity;
            configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

            return MiniMessage.miniMessage().deserialize(configMessage,
                    Placeholder.component("deceased", deceased.displayName()),
                    Placeholder.component("killer", Component.translatable(killer.getType().translationKey())));
        } else {
            String configMessage = configUtils.deathMessageShot;
            configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

            return MiniMessage.miniMessage().deserialize(configMessage,
                    Placeholder.component("deceased", deceased.displayName()));
        }
    }

    private Component handleEntityKill(Player deceased, Entity killer){
        String configMessage = configUtils.deathMessageKilledByEntity;
        configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

        return MiniMessage.miniMessage().deserialize(configMessage,
                Placeholder.component("deceased", deceased.displayName()),
                Placeholder.component("killer", Component.translatable(killer.getType().translationKey())));
    }

    private Component handleOtherCause(Player deceased){
        EntityDamageEvent.DamageCause cause = deceased.getLastDamageCause().getCause();
        String configMessage = switch (cause) {
            case BLOCK_EXPLOSION -> configUtils.deathMessageBlockExplosion;
            case CAMPFIRE -> configUtils.deathMessageCampfire;
            case CONTACT -> configUtils.deathMessageContact;
            case CRAMMING -> configUtils.deathMessageCramming;
            case CUSTOM -> configUtils.deathMessageCustom;
            case DRAGON_BREATH -> configUtils.deathMessageDragonBreath;
            case DROWNING -> configUtils.deathMessageDrowning;
            case ENTITY_EXPLOSION -> configUtils.deathMessageEntityExplosion;
            case FALL -> configUtils.deathMessageFall;
            case FALLING_BLOCK -> configUtils.deathMessageFallingBlock;
            case FIRE -> configUtils.deathMessageFire;
            case FIRE_TICK -> configUtils.deathMessageFireTick;
            case FLY_INTO_WALL -> configUtils.deathMessageFlyIntoWall;
            case FREEZE -> configUtils.deathMessageFreeze;
            case HOT_FLOOR -> configUtils.deathMessageHotFloor;
            case KILL -> configUtils.deathMessageKill;
            case LAVA -> configUtils.deathMessageLava;
            case LIGHTNING -> configUtils.deathMessageLightning;
            case MAGIC -> configUtils.deathMessageMagic;
            case POISON -> configUtils.deathMessagePoison;
            case SONIC_BOOM -> configUtils.deathMessageSonicBoom;
            case STARVATION -> configUtils.deathMessageStarvation;
            case SUFFOCATION -> configUtils.deathMessageSuffocation;
            case SUICIDE -> configUtils.deathMessageSuicide;
            case THORNS -> configUtils.deathMessageThorns;
            case VOID -> configUtils.deathMessageVoid;
            case WITHER -> configUtils.deathMessageWither;
            case WORLD_BORDER -> configUtils.deathMessageWorldBorder;
            default -> configUtils.deathMessageDefault;
        };

        if (configMessage == null){
            configMessage = configUtils.deathMessageDefault;
        }

        configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

        return MiniMessage.miniMessage().deserialize(configMessage,
                Placeholder.component("deceased", deceased.displayName()));
    }
}
