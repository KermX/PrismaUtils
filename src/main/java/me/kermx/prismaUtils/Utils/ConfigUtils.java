package me.kermx.prismaUtils.Utils;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigUtils {

    private final PrismaUtils plugin;
    private final FileConfiguration config;

    public ConfigUtils(PrismaUtils plugin){
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    // General Settings
    public long cooldownDeathMessageSeconds;

    // Death Messages - Players and Entities
    public List<String> deathMessageKilledByPlayer;
    public List<String> deathMessageShotByPlayer;
    public List<String> deathMessageShotByEntity;
    public List<String> deathMessageShot;
    public List<String> deathMessageKilledByEntity;

    public List<String> deathMessageSuffix;

    // Death Messages - Damage Causes
    public List<String> deathMessageBlockExplosion; //new
    public List<String> deathMessageCampfire; //new
    public List<String> deathMessageContact;
    public List<String> deathMessageCramming; //new
    public List<String> deathMessageCustom; //new
    public List<String> deathMessageDragonBreath; //new
    public List<String> deathMessageDrowning;
    public List<String> deathMessageEntityExplosion;
    public List<String> deathMessageFall;
    public List<String> deathMessageFallingBlock; //new
    public List<String> deathMessageFire;
    public List<String> deathMessageFireTick;
    public List<String> deathMessageFlyIntoWall; //new
    public List<String> deathMessageFreeze; //new
    public List<String> deathMessageHotFloor; //new
    public List<String> deathMessageKill; //new
    public List<String> deathMessageLava;
    public List<String> deathMessageLightning;
    public List<String> deathMessageMagic;
    public List<String> deathMessagePoison; //new
    public List<String> deathMessageSonicBoom; //new
    public List<String> deathMessageStarvation;
    public List<String> deathMessageSuffocation;
    public List<String> deathMessageSuicide;
    public List<String> deathMessageThorns;
    public List<String> deathMessageVoid;
    public List<String> deathMessageWither;
    public List<String> deathMessageWorldBorder; //new
    public List<String> deathMessageDefault;

    public void loadConfig(){

        cooldownDeathMessageSeconds = config.getLong("cooldowns.death_message_cooldown_seconds");

        deathMessageSuffix = config.getStringList("death_messages.suffix");

        deathMessageKilledByPlayer = config.getStringList("death_messages.killed_by_player");
        deathMessageShotByPlayer = config.getStringList("death_messages.shot_by_player");
        deathMessageShotByEntity = config.getStringList("death_messages.shot_by_entity");
        deathMessageShot = config.getStringList("death_messages.shot");
        deathMessageKilledByEntity = config.getStringList("death_messages.killed_by_entity");

        deathMessageBlockExplosion = config.getStringList("death_messages.block_explosion");
        deathMessageCampfire = config.getStringList("death_messages.campfire");
        deathMessageContact = config.getStringList("death_messages.contact");
        deathMessageCramming = config.getStringList("death_messages.cramming");
        deathMessageCustom = config.getStringList("death_messages.custom");
        deathMessageDragonBreath = config.getStringList("death_messages.dragon_breath");
        deathMessageDrowning = config.getStringList("death_messages.drowning");
        deathMessageEntityExplosion = config.getStringList("death_messages.entity_explosion");
        deathMessageFall = config.getStringList("death_messages.fall");
        deathMessageFallingBlock = config.getStringList("death_messages.falling_block");
        deathMessageFire = config.getStringList("death_messages.fire");
        deathMessageFireTick = config.getStringList("death_messages.fire_tick");
        deathMessageFlyIntoWall = config.getStringList("death_messages.fly_into_wall");
        deathMessageFreeze = config.getStringList("death_messages.freeze");
        deathMessageHotFloor = config.getStringList("death_messages.hot_floor");
        deathMessageKill = config.getStringList("death_messages.kill");
        deathMessageLava = config.getStringList("death_messages.lava");
        deathMessageLightning = config.getStringList("death_messages.lightning");
        deathMessageMagic = config.getStringList("death_messages.magic");
        deathMessagePoison = config.getStringList("death_messages.poison");
        deathMessageSonicBoom = config.getStringList("death_messages.sonic_boom");
        deathMessageStarvation = config.getStringList("death_messages.starvation");
        deathMessageSuffocation = config.getStringList("death_messages.suffocation");
        deathMessageSuicide = config.getStringList("death_messages.suicide");
        deathMessageThorns = config.getStringList("death_messages.thorns");
        deathMessageVoid = config.getStringList("death_messages.void");
        deathMessageWither = config.getStringList("death_messages.wither");
        deathMessageWorldBorder = config.getStringList("death_messages.world_border");
        deathMessageDefault = config.getStringList("death_messages.default");
    }
}
