package me.kermx.prismaUtils.managers.config;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class DeathMessagesConfigManager {
    private final PrismaUtils plugin;
    private FileConfiguration deathMessages;
    private File deathMessagesFile;

    // Cooldown
    public long deathMessageCooldownSeconds;

    // Death Messages - Players and Entities
    public List<String> deathMessageKilledByPlayer;
    public List<String> deathMessageShotByPlayer;
    public List<String> deathMessageShotByEntity;
    public List<String> deathMessageShot;
    public List<String> deathMessageKilledByEntity;

    public List<String> deathMessageSuffix;

    // Death Messages - Damage Causes
    public List<String> deathMessageBlockExplosion;
    public List<String> deathMessageCampfire;
    public List<String> deathMessageContact;
    public List<String> deathMessageCramming;
    public List<String> deathMessageCustom;
    public List<String> deathMessageDragonBreath;
    public List<String> deathMessageDrowning;
    public List<String> deathMessageEntityExplosion;
    public List<String> deathMessageFall;
    public List<String> deathMessageFallingBlock;
    public List<String> deathMessageFire;
    public List<String> deathMessageFireTick;
    public List<String> deathMessageFlyIntoWall;
    public List<String> deathMessageFreeze;
    public List<String> deathMessageHotFloor;
    public List<String> deathMessageKill;
    public List<String> deathMessageLava;
    public List<String> deathMessageLightning;
    public List<String> deathMessageMagic;
    public List<String> deathMessagePoison;
    public List<String> deathMessageSonicBoom;
    public List<String> deathMessageStarvation;
    public List<String> deathMessageSuffocation;
    public List<String> deathMessageSuicide;
    public List<String> deathMessageThorns;
    public List<String> deathMessageVoid;
    public List<String> deathMessageWither;
    public List<String> deathMessageWorldBorder;
    public List<String> deathMessageDefault;

    public DeathMessagesConfigManager(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        deathMessagesFile = new File(plugin.getDataFolder(), "death_messages.yml");
        if (!deathMessagesFile.exists()) {
            plugin.saveResource("death_messages.yml", false);
        }
        deathMessages = YamlConfiguration.loadConfiguration(deathMessagesFile);

        InputStream defaultStream = plugin.getResource("death_messages.yml");
        if (defaultStream == null) {
            plugin.getLogger().warning("Default death_messages.yml not found in the JAR. No defaults to merge!");
        } else {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            deathMessages.setDefaults(defaultConfig);
            deathMessages.options().copyDefaults(true);

            try {
                deathMessages.save(deathMessagesFile);
                plugin.getLogger().info("Merged any missing keys into death_messages.yml (if needed).");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save merged death_messages.yml!");
                e.printStackTrace();
            }
        }

        deathMessageCooldownSeconds = deathMessages.getInt("cooldown_seconds");

        deathMessageSuffix = deathMessages.getStringList("suffix");

        deathMessageKilledByPlayer = deathMessages.getStringList("killed_by_player");
        deathMessageShotByPlayer = deathMessages.getStringList("shot_by_player");
        deathMessageShotByEntity = deathMessages.getStringList("shot_by_entity");
        deathMessageShot = deathMessages.getStringList("shot");
        deathMessageKilledByEntity = deathMessages.getStringList("killed_by_entity");

        deathMessageBlockExplosion = deathMessages.getStringList("block_explosion");
        deathMessageCampfire = deathMessages.getStringList("campfire");
        deathMessageContact = deathMessages.getStringList("contact");
        deathMessageCramming = deathMessages.getStringList("cramming");
        deathMessageCustom = deathMessages.getStringList("custom");
        deathMessageDragonBreath = deathMessages.getStringList("dragon_breath");
        deathMessageDrowning = deathMessages.getStringList("drowning");
        deathMessageEntityExplosion = deathMessages.getStringList("entity_explosion");
        deathMessageFall = deathMessages.getStringList("fall");
        deathMessageFallingBlock = deathMessages.getStringList("falling_block");
        deathMessageFire = deathMessages.getStringList("fire");
        deathMessageFireTick = deathMessages.getStringList("fire_tick");
        deathMessageFlyIntoWall = deathMessages.getStringList("fly_into_wall");
        deathMessageFreeze = deathMessages.getStringList("freeze");
        deathMessageHotFloor = deathMessages.getStringList("hot_floor");
        deathMessageKill = deathMessages.getStringList("kill");
        deathMessageLava = deathMessages.getStringList("lava");
        deathMessageLightning = deathMessages.getStringList("lightning");
        deathMessageMagic = deathMessages.getStringList("magic");
        deathMessagePoison = deathMessages.getStringList("poison");
        deathMessageSonicBoom = deathMessages.getStringList("sonic_boom");
        deathMessageStarvation = deathMessages.getStringList("starvation");
        deathMessageSuffocation = deathMessages.getStringList("suffocation");
        deathMessageSuicide = deathMessages.getStringList("suicide");
        deathMessageThorns = deathMessages.getStringList("thorns");
        deathMessageVoid = deathMessages.getStringList("void");
        deathMessageWither = deathMessages.getStringList("wither");
        deathMessageWorldBorder = deathMessages.getStringList("world_border");
        deathMessageDefault = deathMessages.getStringList("default");
    }

    public void reload() {
        loadConfig();
    }

    public void save() {
        try {
            deathMessages.save(deathMessagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save death_messages.yml!");
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return deathMessages;
    }
}
