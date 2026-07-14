package me.kermx.prismaUtils.managers.config;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RewardsConfigManager {
    private final PrismaUtils plugin;
    private FileConfiguration config;
    private File configFile;

    private final Map<String, RewardList> rewardLists = new LinkedHashMap<>();

    /**
     * A single rollable reward: a command template and its relative weight.
     */
    public record RewardEntry(String command, int weight) {
    }

    /**
     * A named pool of weighted reward commands.
     */
    public static final class RewardList {
        private final String name;
        private final List<RewardEntry> entries;
        private final int totalWeight;

        public RewardList(String name, List<RewardEntry> entries) {
            this.name = name;
            this.entries = entries;
            int sum = 0;
            for (RewardEntry entry : entries) {
                sum += entry.weight();
            }
            this.totalWeight = sum;
        }

        public String getName() {
            return name;
        }

        public List<RewardEntry> getEntries() {
            return entries;
        }

        /**
         * Pick one entry, weighted by each entry's weight.
         */
        public RewardEntry pickWeighted() {
            if (entries.isEmpty() || totalWeight <= 0) {
                return null;
            }
            int roll = ThreadLocalRandom.current().nextInt(totalWeight);
            int cursor = 0;
            for (RewardEntry entry : entries) {
                cursor += entry.weight();
                if (roll < cursor) {
                    return entry;
                }
            }
            // Shouldn't happen, but fall back to the last entry.
            return entries.get(entries.size() - 1);
        }
    }

    public RewardsConfigManager(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "rewards.yml");
        if (!configFile.exists()) {
            plugin.saveResource("rewards.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Handle defaults and merging like other config managers
        InputStream defaultStream = plugin.getResource("rewards.yml");
        if (defaultStream == null) {
            plugin.getLogger().warning("Default rewards.yml not found in the JAR. No defaults to merge!");
        } else {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);

            try {
                config.save(configFile);
                plugin.getLogger().info("Merged any missing keys into rewards.yml (if needed).");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save merged rewards.yml!");
                e.printStackTrace();
            }
        }

        parseRewardLists();

        plugin.getLogger().info("Loaded " + rewardLists.size() + " reward list(s) from rewards.yml");
    }

    private void parseRewardLists() {
        rewardLists.clear();

        ConfigurationSection root = config.getConfigurationSection("reward-lists");
        if (root == null) {
            return;
        }

        for (String listName : root.getKeys(false)) {
            ConfigurationSection listSection = root.getConfigurationSection(listName);
            if (listSection == null) {
                continue;
            }

            List<RewardEntry> entries = new ArrayList<>();
            List<Map<?, ?>> rawCommands = listSection.getMapList("commands");
            for (Map<?, ?> raw : rawCommands) {
                Object commandObj = raw.get("command");
                if (commandObj == null) {
                    continue;
                }
                String command = commandObj.toString().trim();
                if (command.isEmpty()) {
                    continue;
                }
                // Strip a leading slash so dispatchCommand receives a clean command.
                if (command.startsWith("/")) {
                    command = command.substring(1);
                }

                int weight = 1;
                Object weightObj = raw.get("weight");
                if (weightObj instanceof Number number) {
                    weight = number.intValue();
                }
                if (weight < 1) {
                    plugin.getLogger().warning("Reward in list '" + listName + "' has weight < 1; clamping to 1: " + command);
                    weight = 1;
                }

                entries.add(new RewardEntry(command, weight));
            }

            if (entries.isEmpty()) {
                plugin.getLogger().warning("Reward list '" + listName + "' has no valid commands; skipping.");
                continue;
            }

            rewardLists.put(listName.toLowerCase(), new RewardList(listName, entries));
        }
    }

    /**
     * @return the reward list with the given name (case-insensitive), or null.
     */
    public RewardList getRewardList(String name) {
        if (name == null) {
            return null;
        }
        return rewardLists.get(name.toLowerCase());
    }

    /**
     * @return the names of all configured reward lists (original casing).
     */
    public List<String> getRewardListNames() {
        List<String> names = new ArrayList<>();
        for (RewardList list : rewardLists.values()) {
            names.add(list.getName());
        }
        return names;
    }

    public void reload() {
        loadConfig();
        plugin.getLogger().info("Rewards configuration reloaded.");
    }

    public FileConfiguration getConfig() {
        return config;
    }
}