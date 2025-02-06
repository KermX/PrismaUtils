package me.kermx.prismaUtils.managers.general.configs;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainConfigManager {

    private final PrismaUtils plugin;
    private FileConfiguration config;
    private File configFile;

    // First Join Spawn
    public boolean firstJoinSpawnEnabled;
    public String firstJoinSpawnWorld;
    public double firstJoinSpawnX;
    public double firstJoinSpawnY;
    public double firstJoinSpawnZ;
    public float firstJoinSpawnYaw;
    public float firstJoinSpawnPitch;

    // First Join Commands
    public boolean firstJoinCommandsEnabled;
    public List<String> firstJoinCommands;

    // Health Scale
    public boolean healthScaleEnabled;
    public double healthScaleValue;

    // Remove Crafting Recipes
    public List<String> disabledCraftingRecipes;

    // Tweaks
    public boolean disableSpawnerMobItemDrops;
    public boolean chainsAreClimbable;
    public boolean endermitesImmuneToLightning;
    public boolean enableNonLevelBasedEnchanting;

    public MainConfigManager(PrismaUtils plugin){
        this.plugin = plugin;
    }

    public void loadConfig(){
        if (!plugin.getDataFolder().exists()){
            plugin.getDataFolder().mkdirs();
        }
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()){
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        // First Join Spawn
        firstJoinSpawnEnabled = config.getBoolean("first_join_spawn.enabled");
        firstJoinSpawnWorld = config.getString("first_join_spawn.world");
        firstJoinSpawnX = config.getDouble("first_join_spawn.x");
        firstJoinSpawnY = config.getDouble("first_join_spawn.y");
        firstJoinSpawnZ = config.getDouble("first_join_spawn.z");
        firstJoinSpawnYaw = (float) config.getDouble("first_join_spawn.yaw");
        firstJoinSpawnPitch = (float) config.getDouble("first_join_spawn.pitch");

        // First Join Commands
        firstJoinCommandsEnabled = config.getBoolean("first_join_commands.enabled");
        firstJoinCommands = config.getStringList("first_join_commands.commands");

        // Health Scale
        healthScaleEnabled = config.getBoolean("health_scale.enabled");
        healthScaleValue = config.getDouble("health_scale.value");

        // Remove Crafting Recipes
        disabledCraftingRecipes = config.getStringList("disabled_recipes");

        // Tweaks
        disableSpawnerMobItemDrops = config.getBoolean("tweaks.disable_spawner_mob_item_drops");
        chainsAreClimbable = config.getBoolean("tweaks.chains_are_climbable");
        endermitesImmuneToLightning = config.getBoolean("tweaks.endermites_immune_to_lightning");
        enableNonLevelBasedEnchanting = config.getBoolean("tweaks.non_level_based_enchanting");
    }

    public void reload(){
        loadConfig();
    }

    public void save(){
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml!");
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig(){
        return config;
    }
}
