package me.kermx.prismaUtils.managers.core;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class FeatureToggleManager {

    private final PrismaUtils plugin;
    private final Map<String, Boolean> features = new HashMap<>();
    private FileConfiguration config;
    private File configFile;

    public enum Feature {
        HOMES("systems.homes", "Home system (sethome, home, delhome, adminhome)"),
        WARPS("systems.warps", "Warp system (warp, setwarp, delwarp)"),
        TELEPORT_REQUESTS("systems.teleport_requests", "Teleport request system (tpa, tpahere, tpaccept, tpdeny)"),
        TELEPORT_COMMANDS("systems.teleport_commands", "Teleport commands (tp, tphere, tppos, spawn, back)"),
        AFK_SYSTEM("systems.afk", "AFK system with auto-teleport and protection"),
        FLIGHT_SYSTEM("systems.flight", "Flight system (fly, flytime, tempfly)"),
        MAIL_SYSTEM("systems.mail", "Mail system for player messaging"),
        CHAT_MANAGEMENT("systems.chat", "Chat formatting, filters, and emojis"),
        GOD_MODE("systems.god_mode", "God mode system"),

        // === COMMAND CATEGORIES ===
        CRAFTING_STATIONS("commands.crafting_stations", "Portable crafting stations (anvil, loom, etc)"),
        ADMIN_COMMANDS("commands.admin", "Admin commands (blockinfo, entityinfo, patrol, etc)"),
        UTILITY_COMMANDS("commands.utility", "Utility commands (near, ping, repair, trash, etc)"),
        PLAYER_COMMANDS("commands.player", "Player commands (top, bottom, heal, feed, etc)"),

        // === HANDLERS & TWEAKS ===
        SILK_SPAWNERS("tweaks.silk_spawners", "Allow silk touch on spawners"),
        SPAWNER_MOB_DROPS("tweaks.spawner_mob_drops", "Disable drops from spawner mobs"),
        CLIMBABLE_CHAINS("tweaks.climbable_chains", "Make chains climbable like ladders"),
        HORSE_ZOMBIFICATION("tweaks.horse_zombification", "Convert skeleton/zombie horses with items"),
        NETHER_MOB_ZOMBIFICATION("tweaks.nether_mob_zombification", "Disable piglins turning to zombies"),
        ENDERMITE_LIGHTNING_IMMUNITY("tweaks.endermite_lightning_immunity", "Make endermites immune to lightning"),
        NON_LEVEL_ENCHANTING("tweaks.non_level_enchanting", "Alternative enchanting cost system"),
        COPPER_OXIDATION_CONTROL("tweaks.copper_oxidation", "Control copper oxidation with protection plugins"),
        SEED_SHEAR_BLOCKS("tweaks.seed_shear_blocks", "Seed and shear custom blocks"),
        SLIME_SPLIT_CONTROL("tweaks.slime_split", "Control slime splitting behavior"),
        ANTI_AUTO_FISHING("tweaks.anti_auto_fishing", "Prevent auto-fishing machines"),
        REMOVE_EXCESS_DROPS("tweaks.remove_excess_drops", "Remove excess drops from mobs"),

        // === PLAYER DATA & EVENTS ===
        CUSTOM_DEATH_MESSAGES("features.custom_death_messages", "Custom death messages"),
        HEALTH_SCALE("features.health_scale", "Custom health display scale"),
        FIRST_JOIN_COMMANDS("features.first_join_commands", "Run commands on first join"),
        FIRST_JOIN_SPAWN("features.first_join_spawn", "Teleport to spawn on first join"),
        LAST_LOCATION_TRACKING("features.last_location", "Track player last location for /back"),
        PERMISSION_KEEP_INVENTORY("features.permission_keep_inv", "Permission-based keep inventory"),
        RESPAWN_MESSAGE("features.respawn_message", "Custom respawn messages"),
        SAFE_SPAWN_EGG("features.safe_spawn_egg", "Prevent spawn egg use in protected areas"),

        // === PLACEHOLDERS ===
        PLACEHOLDERAPI("integrations.placeholderapi", "PlaceholderAPI expansions"),

        // === MISC ===
        DISABLED_RECIPES("features.disabled_recipes", "Disable specific crafting recipes"),
        SEEN_SYSTEM("features.seen_system", "Track player seen times");

        private final String configKey;
        private final String description;

        Feature(String configKey, String description) {
            this.configKey = configKey;
            this.description = description;
        }

        public String getConfigKey() {
            return configKey;
        }

        public String getDescription() {
            return description;
        }
    }

    public FeatureToggleManager(PrismaUtils plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "features.yml");
        if (!configFile.exists()) {
            plugin.saveResource("features.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Merge defaults
        InputStream defaultStream = plugin.getResource("features.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);

            try {
                config.save(configFile);
                plugin.getLogger().info("Merged any missing keys into features.yml (if needed).");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save merged features.yml!");
                e.printStackTrace();
            }
        }

        // Load all features into cache
        for (Feature feature : Feature.values()) {
            boolean enabled = config.getBoolean(feature.configKey, true);
            features.put(feature.configKey, enabled);
        }

        plugin.getLogger().info("Loaded " + features.size() + " feature toggles from features.yml");
    }

    /**
     * Check if a feature is enabled
     */
    public boolean isEnabled(Feature feature) {
        return features.getOrDefault(feature.configKey, true);
    }

    /**
     * Enable or disable a feature
     */
    public void setEnabled(Feature feature, boolean enabled) {
        features.put(feature.configKey, enabled);
        config.set(feature.configKey, enabled);
        save();
    }

    /**
     * Reload the features configuration
     */
    public void reload() {
        features.clear();
        loadConfig();
        plugin.getLogger().info("Reloaded feature toggles");
    }

    /**
     * Save the features configuration
     */
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save features.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Get the configuration file
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Get all features and their states
     */
    public Map<Feature, Boolean> getAllFeatures() {
        Map<Feature, Boolean> result = new HashMap<>();
        for (Feature feature : Feature.values()) {
            result.put(feature, isEnabled(feature));
        }
        return result;
    }
}