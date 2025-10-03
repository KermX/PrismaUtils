package me.kermx.prismaUtils.managers.general.configs;

import me.kermx.prismaUtils.PrismaUtils;
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

public class EventPlaceholderConfigManager {
    private final PrismaUtils plugin;
    private FileConfiguration config;
    private File configFile;

    // Settings
    public boolean debug;
    public int cacheDuration;
    public boolean enabled;

    // Events data
    public final Map<String, EventData> events = new HashMap<>();

    public static class EventData {
        private final String name;
        private final String description;
        private final String startDate;
        private final String endDate;
        private final String location;
        private final String host;
        private final int maxParticipants;
        private final String rewards;
        private final String status;

        public EventData(String name, String description, String startDate, String endDate,
                         String location, String host, int maxParticipants, String rewards, String status) {
            this.name = name;
            this.description = description;
            this.startDate = startDate;
            this.endDate = endDate;
            this.location = location;
            this.host = host;
            this.maxParticipants = maxParticipants;
            this.rewards = rewards;
            this.status = status;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getLocation() { return location; }
        public String getHost() { return host; }
        public int getMaxParticipants() { return maxParticipants; }
        public String getRewards() { return rewards; }
        public String getStatus() { return status; }
    }

    public EventPlaceholderConfigManager(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "events.yml");
        if (!configFile.exists()) {
            plugin.saveResource("events.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultStream = plugin.getResource("events.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);

            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save events.yml!");
                e.printStackTrace();
            }
        }

        loadSettings();
        loadEvents();
    }

    private void loadSettings() {
        debug = config.getBoolean("settings.debug", false);
        cacheDuration = config.getInt("settings.cache_duration", 30);
        enabled = config.getBoolean("settings.enabled", true);
    }

    private void loadEvents() {
        events.clear();
        ConfigurationSection eventsSection = config.getConfigurationSection("events");
        if (eventsSection != null) {
            for (String eventKey : eventsSection.getKeys(false)) {
                ConfigurationSection eventSection = eventsSection.getConfigurationSection(eventKey);
                if (eventSection != null) {
                    String name = eventSection.getString("name", "Unknown Event");
                    String description = eventSection.getString("description", "No description");
                    String startDate = eventSection.getString("start_date", "TBD");
                    String endDate = eventSection.getString("end_date", "TBD");
                    String location = eventSection.getString("location", "TBD");
                    String host = eventSection.getString("host", "Server");
                    int maxParticipants = eventSection.getInt("max_participants", 0);
                    String rewards = eventSection.getString("rewards", "None");
                    String status = eventSection.getString("status", "upcoming");

                    events.put(eventKey, new EventData(name, description, startDate, endDate,
                            location, host, maxParticipants, rewards, status));
                }
            }
        }
    }

    public void reload() {
        loadConfig();
        plugin.getLogger().info("Events configuration reloaded.");
    }

    public void save() {
        if (config == null || configFile == null) {
            return;
        }

        try {
            config.set("settings.debug", debug);
            config.set("settings.cache_duration", cacheDuration);
            config.set("settings.enabled", enabled);

            for (Map.Entry<String, EventData> entry : events.entrySet()) {
                String eventKey = "events." + entry.getKey();
                EventData event = entry.getValue();

                config.set(eventKey + ".name", event.getName());
                config.set(eventKey + ".description", event.getDescription());
                config.set(eventKey + ".start_date", event.getStartDate());
                config.set(eventKey + ".end_date", event.getEndDate());
                config.set(eventKey + ".location", event.getLocation());
                config.set(eventKey + ".host", event.getHost());
                config.set(eventKey + ".max_participants", event.getMaxParticipants());
                config.set(eventKey + ".rewards", event.getRewards());
                config.set(eventKey + ".status", event.getStatus());
            }

            config.save(configFile);
            plugin.getLogger().info("Events configuration saved successfully.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save events.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public EventData getEvent(String eventKey) {
        return events.get(eventKey);
    }

    public Set<String> getEventKeys() {
        return events.keySet();
    }

    public boolean hasEvent(String eventKey) {
        return events.containsKey(eventKey);
    }
}
