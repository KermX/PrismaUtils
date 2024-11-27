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
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CustomDeathMessageHandler implements Listener {

    //ADD FEATURES
    // death messages say if someone died while getting attacked by another player or entity
    // randomized death messages, basically each config option is a list of messages and one is chosen at random

    private final ConfigUtils configUtils;
    private final HashMap<UUID, Long> playerDeathTimes = new HashMap<>();
    private final HashMap<UUID, AttackRecord> playerAttackers = new HashMap<>();
    private final Random random = new Random();

    public CustomDeathMessageHandler(ConfigUtils configUtils){
        this.configUtils = configUtils;
    }

    private static class AttackRecord {
        public Entity attacker;
        public long time;

        public AttackRecord(Entity attacker, long time){
            this.attacker = attacker;
            this.time = time;
        }
    }
@EventHandler(priority = EventPriority.MONITOR)
public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
    if (event.getEntity() instanceof Player player){
        long currentTime = System.currentTimeMillis();
        playerAttackers.put(player.getUniqueId(), new AttackRecord(event.getDamager(), currentTime));
    }
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

            AttackRecord previousAttackerRecord = playerAttackers.get(deceased.getUniqueId());

            Component deathMessage = constructDeathMessage(event, deceased, killer, previousAttackerRecord);

            event.deathMessage(deathMessage);
            playerAttackers.remove(deceased.getUniqueId());
        }
    }

    private Component constructDeathMessage(PlayerDeathEvent event, Player deceased, Entity killer, AttackRecord previousAttackerRecord){
        Component message;

        if (killer != null){
            if (killer instanceof Player){
                message = handlePlayerKill(deceased, (Player) killer);
            } else if (killer instanceof Projectile) {
                message = handleProjectileKill(deceased, (Projectile) killer);
            } else {
                message = handleEntityKill(deceased, killer);
            }
        } else {
            message = handleOtherCause(deceased, previousAttackerRecord);
        }
        return message;
    }

    private String getRandomMessage(List<String> messages){
        if (messages == null || messages.isEmpty()){
            return "";
        }
        int index = random.nextInt(messages.size());
        return messages.get(index);
    }

    private Component handlePlayerKill(Player deceased, Player killer){
        String configMessage = getRandomMessage(configUtils.deathMessageKilledByPlayer);
        configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

        return MiniMessage.miniMessage().deserialize(configMessage,
                Placeholder.component("deceased", deceased.displayName()),
                Placeholder.component("killer", killer.displayName()),
                Placeholder.component("weapon", killer.getInventory().getItemInMainHand().displayName()));
    }

    private Component handleProjectileKill(Player deceased, Projectile projectile){
        ProjectileSource shooter = projectile.getShooter();

        if (shooter instanceof Player killer){

            String configMessage = getRandomMessage(configUtils.deathMessageShotByPlayer);
            configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

            return MiniMessage.miniMessage().deserialize(configMessage,
                    Placeholder.component("deceased", deceased.displayName()),
                    Placeholder.component("killer", killer.displayName()),
                    Placeholder.component("weapon", killer.getInventory().getItemInMainHand().displayName()));

        } else if (shooter instanceof Entity killer){
            String configMessage = getRandomMessage(configUtils.deathMessageShotByEntity);
            configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

            return MiniMessage.miniMessage().deserialize(configMessage,
                    Placeholder.component("deceased", deceased.displayName()),
                    Placeholder.component("killer", Component.translatable(killer.getType().translationKey())));
        } else {
            String configMessage = getRandomMessage(configUtils.deathMessageShot);
            configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

            return MiniMessage.miniMessage().deserialize(configMessage,
                    Placeholder.component("deceased", deceased.displayName()));
        }
    }

    private Component handleEntityKill(Player deceased, Entity killer){
        String configMessage = getRandomMessage(configUtils.deathMessageKilledByEntity);
        configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

        return MiniMessage.miniMessage().deserialize(configMessage,
                Placeholder.component("deceased", deceased.displayName()),
                Placeholder.component("killer", Component.translatable(killer.getType().translationKey())));
    }

    private Component handleOtherCause(Player deceased, AttackRecord previousAttackerRecord){
        EntityDamageEvent lastDamageEvent = deceased.getLastDamageCause();
        EntityDamageEvent.DamageCause cause = lastDamageEvent.getCause();

        String configMessage = switch (cause) {
            case BLOCK_EXPLOSION -> getRandomMessage(configUtils.deathMessageBlockExplosion);
            case CAMPFIRE -> getRandomMessage(configUtils.deathMessageCampfire);
            case CONTACT -> getRandomMessage(configUtils.deathMessageContact);
            case CRAMMING -> getRandomMessage(configUtils.deathMessageCramming);
            case CUSTOM -> getRandomMessage(configUtils.deathMessageCustom);
            case DRAGON_BREATH -> getRandomMessage(configUtils.deathMessageDragonBreath);
            case DROWNING -> getRandomMessage(configUtils.deathMessageDrowning);
            case ENTITY_EXPLOSION -> getRandomMessage(configUtils.deathMessageEntityExplosion);
            case FALL -> getRandomMessage(configUtils.deathMessageFall);
            case FALLING_BLOCK -> getRandomMessage(configUtils.deathMessageFallingBlock);
            case FIRE -> getRandomMessage(configUtils.deathMessageFire);
            case FIRE_TICK -> getRandomMessage(configUtils.deathMessageFireTick);
            case FLY_INTO_WALL -> getRandomMessage(configUtils.deathMessageFlyIntoWall);
            case FREEZE -> getRandomMessage(configUtils.deathMessageFreeze);
            case HOT_FLOOR -> getRandomMessage(configUtils.deathMessageHotFloor);
            case KILL -> getRandomMessage(configUtils.deathMessageKill);
            case LAVA -> getRandomMessage(configUtils.deathMessageLava);
            case LIGHTNING -> getRandomMessage(configUtils.deathMessageLightning);
            case MAGIC -> getRandomMessage(configUtils.deathMessageMagic);
            case POISON -> getRandomMessage(configUtils.deathMessagePoison);
            case SONIC_BOOM -> getRandomMessage(configUtils.deathMessageSonicBoom);
            case STARVATION -> getRandomMessage(configUtils.deathMessageStarvation);
            case SUFFOCATION -> getRandomMessage(configUtils.deathMessageSuffocation);
            case SUICIDE -> getRandomMessage(configUtils.deathMessageSuicide);
            case THORNS -> getRandomMessage(configUtils.deathMessageThorns);
            case VOID -> getRandomMessage(configUtils.deathMessageVoid);
            case WITHER -> getRandomMessage(configUtils.deathMessageWither);
            case WORLD_BORDER -> getRandomMessage(configUtils.deathMessageWorldBorder);
            default -> getRandomMessage(configUtils.deathMessageDefault);
        };

        if (configMessage == null || configMessage.isEmpty()){
            configMessage = getRandomMessage(configUtils.deathMessageDefault);
        }

        configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

        Component suffixComponent = Component.text("");
        if (previousAttackerRecord != null){
            Entity previousAttack = previousAttackerRecord.attacker;
            long timeSinceAttack = System.currentTimeMillis() - previousAttackerRecord.time;
            if (timeSinceAttack <= (5 * 1000)){
                Component attackerComponent;
                if (previousAttack instanceof Player playerAttacker){
                    attackerComponent = playerAttacker.displayName();
                } else {
                    attackerComponent = Component.translatable(previousAttack.getType().translationKey());
                }

                suffixComponent = MiniMessage.miniMessage().deserialize(getRandomMessage(configUtils.deathMessageSuffix));
                suffixComponent = suffixComponent.append(attackerComponent);
            }
        }

        return MiniMessage.miniMessage().deserialize(configMessage,
                Placeholder.component("deceased", deceased.displayName()),
                Placeholder.component("suffix", suffixComponent));
    }
}
