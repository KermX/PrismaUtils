package me.kermx.prismaUtils.managers.chat;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.managers.general.configs.ChatConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Pattern;

public class EmojiManager {

    private final PrismaUtils plugin;
    private final ChatConfigManager chatConfig;
    private final Map<String, EmojiData> allEmojis = new HashMap<>();

    // cache for processed messages
    private final Map<String, String> processedMessageCache = new LinkedHashMap<String, String>(100, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 100; // Limit cache size to prevent memory issues
        }
    };

    public EmojiManager(PrismaUtils plugin, ChatConfigManager chatConfig) {
        this.plugin = plugin;
        this.chatConfig = chatConfig;
        loadEmojis();
    }

    /**
     * Load all emojis from config into a single map for efficient processing
     */
    private void loadEmojis() {
        allEmojis.clear();

        for (Map.Entry<String, ChatConfigManager.EmojiCategory> categoryEntry : chatConfig.emojiCategories.entrySet()) {
            String categoryName = categoryEntry.getKey();
            ChatConfigManager.EmojiCategory category = categoryEntry.getValue();

            for (Map.Entry<String, String> emojiEntry : category.getEmojis().entrySet()) {
                String emojiText = chatConfig.emojiCaseSensitive ? emojiEntry.getKey() : emojiEntry.getKey().toLowerCase();
                String emojiUnicode = emojiEntry.getValue();
                String permission = category.getPermission();

                allEmojis.put(emojiText, new EmojiData(emojiUnicode, permission, categoryName));
            }
        }

        // Clear cache when emojis are reloaded
        processedMessageCache.clear();
        plugin.getLogger().info("Loaded " + allEmojis.size() + " emojis from " + chatConfig.emojiCategories.size() + " categories.");
    }

    /**
     * Process emojis in a message
     * @param player The player sending the message
     * @param message The original message (should be checked AFTER chat filters)
     * @return ProcessResult containing the processed message and any permission issues
     */
    public ProcessResult processEmojis(Player player, String message) {
        // Check if emoji system is enabled
        if (!chatConfig.emojiReplacementEnabled) {
            return new ProcessResult(message, true, null);
        }

        // Check default permission if set
        if (!chatConfig.defaultEmojiPermission.isEmpty() && !player.hasPermission(chatConfig.defaultEmojiPermission)) {
            return new ProcessResult(message, true, null);
        }

        // Check cache for performance (but not for permission-sensitive results)
        String cacheKey = message + "|" + player.getUniqueId().toString();
        if (processedMessageCache.containsKey(cacheKey)) {
            String cachedResult = processedMessageCache.get(cacheKey);
            return new ProcessResult(cachedResult, true, null);
        }

        String processedMessage = message;
        int emojiCount = 0;
        String deniedEmoji = null;
        Set<String> processedEmojis = new HashSet<>(); // Track processed emojis to avoid duplicates

        // Sort emojis by length (longest first) to handle overlapping patterns correctly
        // For example, ":smile:" should be processed before ":)" if both exist
        List<Map.Entry<String, EmojiData>> sortedEmojis = new ArrayList<>(allEmojis.entrySet());
        sortedEmojis.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));

        for (Map.Entry<String, EmojiData> entry : sortedEmojis) {
            String emojiText = entry.getKey();
            EmojiData emojiData = entry.getValue();

            // Skip if we've already processed this emoji text in this message
            if (processedEmojis.contains(emojiText)) {
                continue;
            }

            // Create a copy for case-insensitive processing if needed
            String searchMessage = chatConfig.emojiCaseSensitive ? processedMessage : processedMessage.toLowerCase();
            String searchEmoji = chatConfig.emojiCaseSensitive ? emojiText : emojiText.toLowerCase();

            // Check if the emoji text is in the message
            if (searchMessage.contains(searchEmoji)) {
                // Check permission
                if (!emojiData.getPermission().isEmpty() && !player.hasPermission(emojiData.getPermission())) {
                    deniedEmoji = emojiText;
                    continue;
                }

                // Replace emoji (preserve original case in actual replacement)
                String beforeReplacement = processedMessage;
                if (chatConfig.emojiCaseSensitive) {
                    processedMessage = processedMessage.replace(emojiText, emojiData.getUnicode());
                } else {
                    // Case-insensitive replacement
                    processedMessage = processedMessage.replaceAll("(?i)" + Pattern.quote(emojiText), emojiData.getUnicode());
                }

                // Only count if replacement actually occurred
                if (!beforeReplacement.equals(processedMessage)) {
                    emojiCount++;
                    processedEmojis.add(emojiText);

                    // Check max emojis limit
                    if (chatConfig.maxEmojisPerMessage > 0 && emojiCount >= chatConfig.maxEmojisPerMessage) {
                        break;
                    }

                    // If not processing all matches, stop after first replacement
                    if (!chatConfig.processAllEmojiMatches) {
                        break;
                    }
                }
            }
        }

        // Cache successful results (but not permission-denied ones)
        if (deniedEmoji == null && emojiCount > 0) {
            processedMessageCache.put(cacheKey, processedMessage);
        }

        return new ProcessResult(processedMessage, deniedEmoji == null, deniedEmoji);
    }

    /**
     * Send permission denied message to player
     * @param player The player to notify
     * @param deniedEmoji The emoji they don't have permission for
     */
    public void notifyPermissionDenied(Player player, String deniedEmoji) {
        EmojiData emojiData = allEmojis.get(chatConfig.emojiCaseSensitive ? deniedEmoji : deniedEmoji.toLowerCase());
        String permission = emojiData != null ? emojiData.getPermission() : "unknown";

        String message = chatConfig.emojiNoPermissionMessage
                .replace("<emoji>", deniedEmoji)
                .replace("<permission>", permission);

        player.sendMessage(TextUtils.deserializeString(message));
    }

    /**
     * Reload emojis (called when config is reloaded)
     */
    public void reload() {
        loadEmojis();
    }

    /**
     * Get all available emojis for a player (for commands or UI)
     * @param player The player to check permissions for
     * @return Map of emoji text to unicode for emojis the player can use
     */
    public Map<String, String> getAvailableEmojis(Player player) {
        Map<String, String> availableEmojis = new HashMap<>();

        // Check default permission
        if (!chatConfig.defaultEmojiPermission.isEmpty() && !player.hasPermission(chatConfig.defaultEmojiPermission)) {
            return availableEmojis;
        }

        for (Map.Entry<String, EmojiData> entry : allEmojis.entrySet()) {
            String emojiText = entry.getKey();
            EmojiData emojiData = entry.getValue();

            if (emojiData.getPermission().isEmpty() || player.hasPermission(emojiData.getPermission())) {
                availableEmojis.put(emojiText, emojiData.getUnicode());
            }
        }

        return availableEmojis;
    }

    /**
     * Data class for emoji information
     */
    private static class EmojiData {
        private final String unicode;
        private final String permission;
        private final String category;

        public EmojiData(String unicode, String permission, String category) {
            this.unicode = unicode;
            this.permission = permission;
            this.category = category;
        }

        public String getUnicode() { return unicode; }
        public String getPermission() { return permission; }
        public String getCategory() { return category; }
    }

    /**
     * Result of emoji processing
     */
    public static class ProcessResult {
        private final String processedMessage;
        private final boolean success;
        private final String deniedEmoji;

        public ProcessResult(String processedMessage, boolean success, String deniedEmoji) {
            this.processedMessage = processedMessage;
            this.success = success;
            this.deniedEmoji = deniedEmoji;
        }

        public String getProcessedMessage() { return processedMessage; }
        public boolean isSuccess() { return success; }
        public String getDeniedEmoji() { return deniedEmoji; }
    }
}
