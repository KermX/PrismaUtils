package me.kermx.prismaUtils.managers.config;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WarpsConfigManager {
    private final PrismaUtils plugin;
    private FileConfiguration config;
    private File configFile;

    public static class WarpData {
        private final Location location;
        private final String permission;

        public WarpData(Location location, String permission) {
            this.location = location;
            this.permission = permission;
        }
        public Location getLocation() {return location;}
        public String getPermission() {return permission;}
    }

    private final Map<String, WarpData> warps = new HashMap<>();

    public WarpsConfigManager(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        configFile = new File(plugin.getDataFolder(), "warps.yml");
        if (!configFile.exists()) {
            plugin.saveResource("warps.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultStream = plugin.getResource("warps.yml");
        if (defaultStream == null) {
            plugin.getLogger().warning("Default warps.yml not found in the JAR. No defaults to merge!");
        } else {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);

            try {
                config.save(configFile);
                plugin.getLogger().info("Merged any missing keys into warps.yml (if needed).");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save merged warps.yml!");
                e.printStackTrace();
            }
        }

        // Delay loading warps to ensure all worlds are loaded
        Bukkit.getScheduler().runTaskLater(plugin, this::loadWarps, 60L); // 1 second delay
    }

    private void loadWarps() {
        ConfigurationSection warpsSection = config.getConfigurationSection("warps");
        if (warpsSection == null) return;

        warps.clear(); // Clear existing warps in case of reload

        for (String warpName : warpsSection.getKeys(false)) {
            String worldName = warpsSection.getString(warpName + ".world");
            double x = warpsSection.getDouble(warpName + ".x");
            double y = warpsSection.getDouble(warpName + ".y");
            double z = warpsSection.getDouble(warpName + ".z");
            float yaw = (float) warpsSection.getDouble(warpName + ".yaw");
            float pitch = (float) warpsSection.getDouble(warpName + ".pitch");
            String permission = warpsSection.getString(warpName + ".permission");

            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                Location location = new Location(world, x, y, z, yaw, pitch);
                warps.put(warpName.toLowerCase(), new WarpData(location, permission));
            } else {
                plugin.getLogger().warning("Could not load warp '" + warpName + "' because world '" + worldName + "' does not exist.");
            }
        }
        plugin.getLogger().info("Loaded " + warps.size() + " warps from config.");
    }

    public void setWarp(String name, Location location) {
        setWarp(name, location, null);
    }

    public void setWarp(String name, Location location, String permission) {
        String warpName = name.toLowerCase();
        warps.put(warpName, new WarpData(location.clone(), permission));

        // Save to config
        config.set("warps." + warpName + ".world", location.getWorld().getName());
        config.set("warps." + warpName + ".x", location.getX());
        config.set("warps." + warpName + ".y", location.getY());
        config.set("warps." + warpName + ".z", location.getZ());
        config.set("warps." + warpName + ".yaw", location.getYaw());
        config.set("warps." + warpName + ".pitch", location.getPitch());

        if (permission != null && !permission.isEmpty()) {
            config.set("warps." + warpName + ".permission", permission);
        } else {
            config.set("warps." + warpName + ".permission", null);
        }

        save();
    }

    public boolean deleteWarp(String name) {
        String warpName = name.toLowerCase();
        if (warps.containsKey(warpName)) {
            warps.remove(warpName);
            config.set("warps." + warpName, null);
            save();
            return true;
        }
        return false;
    }

    public Location getWarp(String name) {
        WarpData warpData = warps.get(name.toLowerCase());
        return warpData != null ? warpData.getLocation() : null;
    }

    public String getWarpPermission(String name) {
        WarpData warpData = warps.get(name.toLowerCase());
        return warpData != null ? warpData.getPermission() : null;
    }

    public WarpData getWarpData(String name) {
        return warps.get(name.toLowerCase());
    }

    public Set<String> getWarpNames() {
        return warps.keySet();
    }

    public boolean warpExists(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    public void reload() {
        loadConfig();
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save warps.yml!");
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
