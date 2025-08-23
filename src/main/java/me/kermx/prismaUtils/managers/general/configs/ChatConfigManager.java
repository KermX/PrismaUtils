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
import java.util.List;
import java.util.Map;

public class ChatConfigManager {

    private final PrismaUtils plugin;
    private FileConfiguration config;
    private File configFile;

    // Chat Filter Configuration
    public boolean chatFilterEnabled;
    public String staffNotificationPermission;
    public String bypassPermission;
    public List<String> monitoredCommands;
    public final Map<String, FilterData> filters = new HashMap<>();

    // Emoji Replacement Configuration
    public boolean emojiReplacementEnabled;
    public String defaultEmojiPermission;
    public final Map<String, EmojiCategory> emojiCategories = new HashMap<>();

    // Messages
    public String staffNotificationMessage;
    public String playerFilteredMessage;
    public String emojiNoPermissionMessage;

    // Advanced Settings
    public boolean logFilteredMessages;
    public boolean emojiCaseSensitive;
    public int maxEmojisPerMessage;
    public boolean processAllEmojiMatches;
    public int filterCooldown;

    public static class FilterData {
        private final boolean enabled;
        private final List<String> patterns;
        private final String reason;

        public FilterData(boolean enabled, List<String> patterns, String reason) {
            this.enabled = enabled;
            this.patterns = patterns;
            this.reason = reason;
        }

        public boolean isEnabled() { return enabled; }
        public List<String> getPatterns() { return patterns; }
        public String getReason() { return reason; }
    }

    public static class EmojiCategory {
        private final String permission;
        private final Map<String, String> emojis;

        public EmojiCategory(String permission, Map<String, String> emojis) {
            this.permission = permission;
            this.emojis = emojis;
        }

        public String getPermission() { return permission; }
        public Map<String, String> getEmojis() { return emojis; }
    }

    public ChatConfigManager(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        configFile = new File(plugin.getDataFolder(), "chat.yml");
        if (!configFile.exists()) {
            plugin.saveResource("chat.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultStream = plugin.getResource("chat.yml");
        if (defaultStream == null) {
            plugin.getLogger().warning("Default chat.yml not found in the JAR. No defaults to merge!");
        } else {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);

            try {
                config.save(configFile);
                plugin.getLogger().info("Merged any missing keys into chat.yml (if needed).");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save merged chat.yml!");
                e.printStackTrace();
            }
        }

        loadChatFilterConfig();
        loadEmojiReplacementConfig();
        loadMessagesConfig();
        loadAdvancedConfig();
    }

    private void loadChatFilterConfig() {
        chatFilterEnabled = config.getBoolean("chat_filter.enabled", true);
        staffNotificationPermission = config.getString("chat_filter.staff_notification_permission", "prismautils.chatfilter.notify");
        bypassPermission = config.getString("chat_filter.bypass_permission", "prismautils.chatfilter.bypass");
        monitoredCommands = config.getStringList("chat_filter.monitored_commands");

        filters.clear();
        ConfigurationSection filtersSection = config.getConfigurationSection("chat_filter.filters");
        if (filtersSection != null) {
            for (String filterName : filtersSection.getKeys(false)) {
                boolean enabled = filtersSection.getBoolean(filterName + ".enabled", true);
                List<String> patterns = filtersSection.getStringList(filterName + ".patterns");
                String reason = filtersSection.getString(filterName + ".reason", filterName + " Filter");

                filters.put(filterName, new FilterData(enabled, patterns, reason));
            }
        }
    }

    private void loadEmojiReplacementConfig() {
        emojiReplacementEnabled = config.getBoolean("emoji_replacement.enabled", true);
        defaultEmojiPermission = config.getString("emoji_replacement.default_permission", "");

        emojiCategories.clear();
        ConfigurationSection categoriesSection = config.getConfigurationSection("emoji_replacement.categories");
        if (categoriesSection != null) {
            for (String categoryName : categoriesSection.getKeys(false)) {
                String permission = categoriesSection.getString(categoryName + ".permission", "");

                Map<String, String> emojis = new HashMap<>();
                ConfigurationSection emojisSection = categoriesSection.getConfigurationSection(categoryName + ".emojis");
                if (emojisSection != null) {
                    for (String emojiKey : emojisSection.getKeys(false)) {
                        String emojiValue = emojisSection.getString(emojiKey);
                        if (emojiValue != null) {
                            emojis.put(emojiKey, emojiValue);
                        }
                    }
                }

                emojiCategories.put(categoryName, new EmojiCategory(permission, emojis));
            }
        }
    }

    private void loadMessagesConfig() {
        staffNotificationMessage = config.getString("messages.staff_notification",
                "<red>[ChatFilter] <gray>Player <yellow><player> <gray>triggered filter <red><filter_name><gray>: <white><message>");
        playerFilteredMessage = config.getString("messages.player_filtered_message",
                "<red>Your message was filtered and not sent.");
        emojiNoPermissionMessage = config.getString("messages.emoji_no_permission",
                "<red>You don't have permission to use the emoji: <white><emoji>");
    }

    private void loadAdvancedConfig() {
        logFilteredMessages = config.getBoolean("advanced.log_filtered_messages", true);
        emojiCaseSensitive = config.getBoolean("advanced.emoji_case_sensitive", false);
        maxEmojisPerMessage = config.getInt("advanced.max_emojis_per_message", 10);
        processAllEmojiMatches = config.getBoolean("advanced.process_all_emoji_matches", true);
        filterCooldown = config.getInt("advanced.filter_cooldown", 5);
    }

    public void reload() {
        loadConfig();
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save chat.yml!");
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    // Convenience methods for accessing filter data
    public FilterData getFilter(String filterName) {
        return filters.get(filterName);
    }

    public boolean isFilterEnabled(String filterName) {
        FilterData filter = filters.get(filterName);
        return filter != null && filter.isEnabled();
    }

    // Convenience methods for accessing emoji data
    public EmojiCategory getEmojiCategory(String categoryName) {
        return emojiCategories.get(categoryName);
    }

    public Map<String, String> getAllEmojis() {
        Map<String, String> allEmojis = new HashMap<>();
        for (EmojiCategory category : emojiCategories.values()) {
            allEmojis.putAll(category.getEmojis());
        }
        return allEmojis;
    }

    public String getEmojiPermission(String emojiText) {
        for (EmojiCategory category : emojiCategories.values()) {
            if (category.getEmojis().containsKey(emojiText)) {
                return category.getPermission();
            }
        }
        return null;
    }
}

