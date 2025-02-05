package me.kermx.prismaUtils.handlers.player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.kermx.prismaUtils.managers.general.ConfigManager;
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

    private final HashMap<UUID, Long> playerDeathTimes = new HashMap<>();
    private final HashMap<UUID, AttackRecord> playerAttackers = new HashMap<>();
    private final Random random = new Random();


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


    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event){
        Player deceased = event.getEntity();

        long currentTime = System.currentTimeMillis();
        Long lastDeathTime = playerDeathTimes.get(deceased.getUniqueId());
        long cooldownPeriod = ConfigManager.getInstance().cooldownDeathMessageSeconds * 1000;

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
        String configMessage = getRandomMessage(ConfigManager.getInstance().deathMessageKilledByPlayer);
        configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

        return MiniMessage.miniMessage().deserialize(configMessage,
                Placeholder.component("deceased", deceased.displayName()),
                Placeholder.component("killer", killer.displayName()),
                Placeholder.component("weapon", killer.getInventory().getItemInMainHand().displayName()));
    }

    private Component handleProjectileKill(Player deceased, Projectile projectile){
        ProjectileSource shooter = projectile.getShooter();

        if (shooter instanceof Player killer){

            String configMessage = getRandomMessage(ConfigManager.getInstance().deathMessageShotByPlayer);
            configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

            return MiniMessage.miniMessage().deserialize(configMessage,
                    Placeholder.component("deceased", deceased.displayName()),
                    Placeholder.component("killer", killer.displayName()),
                    Placeholder.component("weapon", killer.getInventory().getItemInMainHand().displayName()));

        } else if (shooter instanceof Entity killer){
            String configMessage = getRandomMessage(ConfigManager.getInstance().deathMessageShotByEntity);
            configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

            return MiniMessage.miniMessage().deserialize(configMessage,
                    Placeholder.component("deceased", deceased.displayName()),
                    Placeholder.component("killer", Component.translatable(killer.getType().translationKey())));
        } else {
            String configMessage = getRandomMessage(ConfigManager.getInstance().deathMessageShot);
            configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

            return MiniMessage.miniMessage().deserialize(configMessage,
                    Placeholder.component("deceased", deceased.displayName()));
        }
    }

    private Component handleEntityKill(Player deceased, Entity killer){
        String configMessage = getRandomMessage(ConfigManager.getInstance().deathMessageKilledByEntity);
        configMessage = PlaceholderAPI.setPlaceholders(deceased, configMessage);

        return MiniMessage.miniMessage().deserialize(configMessage,
                Placeholder.component("deceased", deceased.displayName()),
                Placeholder.component("killer", Component.translatable(killer.getType().translationKey())));
    }

    private Component handleOtherCause(Player deceased, AttackRecord previousAttackerRecord){
        EntityDamageEvent lastDamageEvent = deceased.getLastDamageCause();
        EntityDamageEvent.DamageCause cause = lastDamageEvent.getCause();

        String configMessage = switch (cause) {
            case BLOCK_EXPLOSION -> getRandomMessage(ConfigManager.getInstance().deathMessageBlockExplosion);
            case CAMPFIRE -> getRandomMessage(ConfigManager.getInstance().deathMessageCampfire);
            case CONTACT -> getRandomMessage(ConfigManager.getInstance().deathMessageContact);
            case CRAMMING -> getRandomMessage(ConfigManager.getInstance().deathMessageCramming);
            case CUSTOM -> getRandomMessage(ConfigManager.getInstance().deathMessageCustom);
            case DRAGON_BREATH -> getRandomMessage(ConfigManager.getInstance().deathMessageDragonBreath);
            case DROWNING -> getRandomMessage(ConfigManager.getInstance().deathMessageDrowning);
            case ENTITY_EXPLOSION -> getRandomMessage(ConfigManager.getInstance().deathMessageEntityExplosion);
            case FALL -> getRandomMessage(ConfigManager.getInstance().deathMessageFall);
            case FALLING_BLOCK -> getRandomMessage(ConfigManager.getInstance().deathMessageFallingBlock);
            case FIRE -> getRandomMessage(ConfigManager.getInstance().deathMessageFire);
            case FIRE_TICK -> getRandomMessage(ConfigManager.getInstance().deathMessageFireTick);
            case FLY_INTO_WALL -> getRandomMessage(ConfigManager.getInstance().deathMessageFlyIntoWall);
            case FREEZE -> getRandomMessage(ConfigManager.getInstance().deathMessageFreeze);
            case HOT_FLOOR -> getRandomMessage(ConfigManager.getInstance().deathMessageHotFloor);
            case KILL -> getRandomMessage(ConfigManager.getInstance().deathMessageKill);
            case LAVA -> getRandomMessage(ConfigManager.getInstance().deathMessageLava);
            case LIGHTNING -> getRandomMessage(ConfigManager.getInstance().deathMessageLightning);
            case MAGIC -> getRandomMessage(ConfigManager.getInstance().deathMessageMagic);
            case POISON -> getRandomMessage(ConfigManager.getInstance().deathMessagePoison);
            case SONIC_BOOM -> getRandomMessage(ConfigManager.getInstance().deathMessageSonicBoom);
            case STARVATION -> getRandomMessage(ConfigManager.getInstance().deathMessageStarvation);
            case SUFFOCATION -> getRandomMessage(ConfigManager.getInstance().deathMessageSuffocation);
            case SUICIDE -> getRandomMessage(ConfigManager.getInstance().deathMessageSuicide);
            case THORNS -> getRandomMessage(ConfigManager.getInstance().deathMessageThorns);
            case VOID -> getRandomMessage(ConfigManager.getInstance().deathMessageVoid);
            case WITHER -> getRandomMessage(ConfigManager.getInstance().deathMessageWither);
            case WORLD_BORDER -> getRandomMessage(ConfigManager.getInstance().deathMessageWorldBorder);
            default -> getRandomMessage(ConfigManager.getInstance().deathMessageDefault);
        };

        if (configMessage == null || configMessage.isEmpty()){
            configMessage = getRandomMessage(ConfigManager.getInstance().deathMessageDefault);
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

                suffixComponent = MiniMessage.miniMessage().deserialize(getRandomMessage(ConfigManager.getInstance().deathMessageSuffix));
                suffixComponent = suffixComponent.append(attackerComponent);
            }
        }

        return MiniMessage.miniMessage().deserialize(configMessage,
                Placeholder.component("deceased", deceased.displayName()),
                Placeholder.component("suffix", suffixComponent));
    }
}
