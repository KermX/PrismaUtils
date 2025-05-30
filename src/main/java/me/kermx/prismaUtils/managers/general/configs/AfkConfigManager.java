package me.kermx.prismaUtils.managers.general.configs;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AfkConfigManager {
    private final PrismaUtils plugin;
    private FileConfiguration config;
    private File configFile;

    // AFK Settings
    public boolean afkEnabled;
    public int afkThresholdSeconds;
    public int afkTeleportAfterSeconds;
    public Location afkLocation;
    public double locationRadius;
    public boolean broadcastAfkMessages;
    public boolean disableAfkDamage;
    public boolean excludeCreativePlayersFromAfk;
    public boolean excludeSpectatorPlayersFromAfk;
    public String afkMessage;
    public String afkReturnMessage;

    public AfkConfigManager(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "afk.yml");
        if (!configFile.exists()) {
            plugin.saveResource("afk.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Handle defaults and merging like other config managers
        InputStream defaultStream = plugin.getResource("afk.yml");
        if (defaultStream == null) {
            plugin.getLogger().warning("Default afk.yml not found in the JAR. No defaults to merge!");
        } else {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);

            try {
                config.save(configFile);
                plugin.getLogger().info("Merged any missing keys into afk.yml (if needed).");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save merged afk.yml!");
                e.printStackTrace();
            }
        }

        // Load AFK Settings
        afkEnabled = config.getBoolean("afk.enabled", true);
        afkThresholdSeconds = config.getInt("afk.threshold_seconds", 300); // 5 minutes default
        afkTeleportAfterSeconds = config.getInt("afk.teleport_after_seconds", 600); // 10 minutes default
        broadcastAfkMessages = config.getBoolean("afk.broadcast_messages", true);
        disableAfkDamage = config.getBoolean("afk.disable_damage", true);
        excludeCreativePlayersFromAfk = config.getBoolean("afk.exclude_creative_players", true);
        excludeSpectatorPlayersFromAfk = config.getBoolean("afk.exclude_spectator_players", true);
        afkMessage = config.getString("afk.message", "<red><player> is now AFK!");
        afkReturnMessage = config.getString("afk.return_message", "<red><player> is no longer AFK!");
        locationRadius = config.getDouble("afk.location_radius", 3);

        // Load AFK Location
        String worldName = config.getString("afk.location.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            world = Bukkit.getWorlds().get(0);
            plugin.getLogger().warning("AFK world '" + worldName + "' not found, using default world instead.");
        }

        double x = config.getDouble("afk.location.x", 0);
        double y = config.getDouble("afk.location.y", 100);
        double z = config.getDouble("afk.location.z", 0);
        float yaw = (float) config.getDouble("afk.location.yaw", 0);
        float pitch = (float) config.getDouble("afk.location.pitch", 0);

        afkLocation = new Location(world, x, y, z, yaw, pitch);

        plugin.getLogger().info("AFK configuration loaded successfully.");
    }

    public void reload() {
        loadConfig();
        plugin.getLogger().info("AFK configuration reloaded.");
    }

    public void save() {
        if (config == null || configFile == null) {
            return;
        }

        try {
            // Save AFK Location
            if (afkLocation != null) {
                config.set("afk.location.world", afkLocation.getWorld().getName());
                config.set("afk.location.x", afkLocation.getX());
                config.set("afk.location.y", afkLocation.getY());
                config.set("afk.location.z", afkLocation.getZ());
                config.set("afk.location.yaw", afkLocation.getYaw());
                config.set("afk.location.pitch", afkLocation.getPitch());
            }

            // Save other settings
            config.set("afk.enabled", afkEnabled);
            config.set("afk.threshold_seconds", afkThresholdSeconds);
            config.set("afk.teleport_after_seconds", afkTeleportAfterSeconds);
            config.set("afk.broadcast_messages", broadcastAfkMessages);
            config.set("afk.disable_damage", disableAfkDamage);
            config.set("afk.exclude_creative_players", excludeCreativePlayersFromAfk);
            config.set("afk.exclude_spectator_players", excludeSpectatorPlayersFromAfk);
            config.set("afk.message", afkMessage);
            config.set("afk.return_message", afkReturnMessage);
            config.set("afk.location_radius", locationRadius);

            config.save(configFile);
            plugin.getLogger().info("AFK configuration saved successfully.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save afk.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setAfkLocation(Location location) {
        if (location != null) {
            this.afkLocation = location.clone();
            save();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }
}