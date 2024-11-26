package me.kermx.prismaUtils.Utils;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.configuration.file.FileConfiguration;

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
    public String deathMessageKilledByPlayer;
    public String deathMessageShotByPlayer;
    public String deathMessageShotByEntity;
    public String deathMessageShot;
    public String deathMessageKilledByEntity;

    // Death Messages - Damage Causes
    public String deathMessageBlockExplosion; //new
    public String deathMessageCampfire; //new
    public String deathMessageContact;
    public String deathMessageCramming; //new
    public String deathMessageCustom; //new
    public String deathMessageDragonBreath; //new
    public String deathMessageDrowning;
    public String deathMessageEntityExplosion;
    public String deathMessageFall;
    public String deathMessageFallingBlock; //new
    public String deathMessageFire;
    public String deathMessageFireTick;
    public String deathMessageFlyIntoWall; //new
    public String deathMessageFreeze; //new
    public String deathMessageHotFloor; //new
    public String deathMessageKill; //new
    public String deathMessageLava;
    public String deathMessageLightning;
    public String deathMessageMagic;
    public String deathMessagePoison; //new
    public String deathMessageSonicBoom; //new
    public String deathMessageStarvation;
    public String deathMessageSuffocation;
    public String deathMessageSuicide;
    public String deathMessageThorns;
    public String deathMessageVoid;
    public String deathMessageWither;
    public String deathMessageWorldBorder; //new
    public String deathMessageDefault;

    public void loadConfig(){

        cooldownDeathMessageSeconds = config.getLong("cooldowns.death_message_cooldown_seconds");

        deathMessageKilledByPlayer = config.getString("death_messages.killed_by_player");
        deathMessageShotByPlayer = config.getString("death_messages.shot_by_player");
        deathMessageShotByEntity = config.getString("death_messages.shot_by_entity");
        deathMessageShot = config.getString("death_messages.shot");
        deathMessageKilledByEntity = config.getString("death_messages.killed_by_entity");

        deathMessageBlockExplosion = config.getString("death_messages.block_explosion");
        deathMessageCampfire = config.getString("death_messages.campfire");
        deathMessageContact = config.getString("death_messages.contact");
        deathMessageCramming = config.getString("death_messages.cramming");
        deathMessageCustom = config.getString("death_messages.custom");
        deathMessageDragonBreath = config.getString("death_messages.dragon_breath");
        deathMessageDrowning = config.getString("death_messages.drowning");
        deathMessageEntityExplosion = config.getString("death_messages.entity_explosion");
        deathMessageFall = config.getString("death_messages.fall");
        deathMessageFallingBlock = config.getString("death_messages.falling_block");
        deathMessageFire = config.getString("death_messages.fire");
        deathMessageFireTick = config.getString("death_messages.fire_tick");
        deathMessageFlyIntoWall = config.getString("death_messages.fly_into_wall");
        deathMessageFreeze = config.getString("death_messages.freeze");
        deathMessageHotFloor = config.getString("death_messages.hot_floor");
        deathMessageKill = config.getString("death_messages.kill");
        deathMessageLava = config.getString("death_messages.lava");
        deathMessageLightning = config.getString("death_messages.lightning");
        deathMessageMagic = config.getString("death_messages.magic");
        deathMessagePoison = config.getString("death_messages.poison");
        deathMessageSonicBoom = config.getString("death_messages.sonic_boom");
        deathMessageStarvation = config.getString("death_messages.starvation");
        deathMessageSuffocation = config.getString("death_messages.suffocation");
        deathMessageSuicide = config.getString("death_messages.suicide");
        deathMessageThorns = config.getString("death_messages.thorns");
        deathMessageVoid = config.getString("death_messages.void");
        deathMessageWither = config.getString("death_messages.wither");
        deathMessageWorldBorder = config.getString("death_messages.world_border");
        deathMessageDefault = config.getString("death_messages.default");
    }
}
