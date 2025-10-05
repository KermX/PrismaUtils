package me.kermx.prismaUtils.managers.chat;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.managers.config.ChatConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class ChatFilterManager {

    private final PrismaUtils plugin;
    private final ChatConfigManager chatConfig;
    private final Map<String, Pattern[]> compiledFilters = new HashMap<>();
    private final Map<UUID, Long> filterCooldowns = new HashMap<>();

    public ChatFilterManager(PrismaUtils plugin, ChatConfigManager chatConfig) {
        this.plugin = plugin;
        this.chatConfig = chatConfig;
        compileFilters();
    }

    /**
     * Pre-compile all regex patterns for better performance
     */
    private void compileFilters() {
        compiledFilters.clear();

        for (Map.Entry<String, ChatConfigManager.FilterData> entry : chatConfig.filters.entrySet()) {
            String filterName = entry.getKey();
            ChatConfigManager.FilterData filterData = entry.getValue();

            if (!filterData.isEnabled()) {
                continue;
            }

            Pattern[] patterns = new Pattern[filterData.getPatterns().size()];
            for (int i = 0; i < filterData.getPatterns().size(); i++) {
                try {
                    patterns[i] = Pattern.compile(filterData.getPatterns().get(i));
                } catch (PatternSyntaxException e) {
                    plugin.getLogger().warning("Invalid regex pattern in filter '" + filterName + "': " + filterData.getPatterns().get(i));
                    plugin.getLogger().warning("Error: " + e.getMessage());
                }
            }
            compiledFilters.put(filterName, patterns);
        }

        plugin.getLogger().info("Compiled " + compiledFilters.size() + " chat filters.");
    }

    /**
     * Check if a message should be filtered
     * @param player The player sending the message
     * @param message The message to check
     * @return FilterResult containing whether it's filtered and which filter matched
     */
    public FilterResult checkMessage(Player player, String message) {
        // Check if chat filter is enabled
        if (!chatConfig.chatFilterEnabled) {
            return new FilterResult(false, null, null, null, null);
        }

        // Check bypass permission
        if (player.hasPermission(chatConfig.bypassPermission)) {
            return new FilterResult(false, null, null, null, null);
        }

        // Check cooldown
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Long lastFiltered = filterCooldowns.get(playerId);

        if (lastFiltered != null && (currentTime - lastFiltered) < (chatConfig.filterCooldown * 1000L)) {
            return new FilterResult(true, "cooldown", "Message sent too soon after previous filtered message", null, null);
        }

        // Check each filter
        for (Map.Entry<String, Pattern[]> entry : compiledFilters.entrySet()) {
            String filterName = entry.getKey();
            Pattern[] patterns = entry.getValue();

            for (Pattern pattern : patterns) {
                if (pattern != null) {
                    Matcher matcher = pattern.matcher(message);
                    if (matcher.find()) {
                        String matchedText = matcher.group();

                        // Set cooldown
                        filterCooldowns.put(playerId, currentTime);

                        ChatConfigManager.FilterData filterData = chatConfig.getFilter(filterName);
                        String reason = filterData != null ? filterData.getReason() : filterName + " Filter";

                        return new FilterResult(true, filterName, reason, matchedText, pattern);
                    }
                }
            }
        }

        return new FilterResult(false, null, null, null, null);
    }


    /**
     * Notify staff members about a filtered message with highlighted matches
     * @param player The player who sent the filtered message
     * @param originalMessage The original message that was filtered
     * @param filterName The name of the filter that caught it
     * @param reason The reason for filtering
     * @param matchedText The specific text that matched the filter (for highlighting)
     * @param pattern The regex pattern that was matched (for accurate highlighting)
     */
    public void notifyStaff(Player player, String originalMessage, String filterName, String reason, String matchedText, Pattern pattern) {
        if (chatConfig.staffNotificationPermission.isEmpty()) {
            return;
        }

        // Create highlighted message
        String highlightedMessage = highlightMatchedText(originalMessage, pattern);

        String notificationMessage = chatConfig.staffNotificationMessage
                .replace("<player>", player.getName())
                .replace("<message>", highlightedMessage)
                .replace("<filter_name>", filterName)
                .replace("<reason>", reason);

        // Send to all online staff
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission(chatConfig.staffNotificationPermission)) {
                staff.sendMessage(TextUtils.deserializeString(notificationMessage));
            }
        }

        // Log to console if enabled (without color formatting for console)
        if (chatConfig.logFilteredMessages) {
            plugin.getLogger().info("Chat Filter [" + filterName + "] blocked message from " +
                    player.getName() + ": " + originalMessage + " | Matched: '" + matchedText + "'");
        }
    }

    /**
     * Convenience method for backward compatibility
     */
    public void notifyStaff(Player player, String originalMessage, String filterName, String reason) {
        notifyStaff(player, originalMessage, filterName, reason, null, null);
    }

    /**
     * Highlight the matched text in the original message using the original regex pattern
     * @param originalMessage The original message
     * @param pattern The regex pattern that matched (for accurate highlighting)
     * @return The message with highlighted matched text
     */
    private String highlightMatchedText(String originalMessage, Pattern pattern) {
        if (pattern == null) {
            return originalMessage;
        }

        // Use the original pattern to replace matches with highlighted versions
        return pattern.matcher(originalMessage).replaceAll(matchResult -> {
            String match = matchResult.group();
            return "<bold><gold>" + match + "</gold></bold>";
        });
    }


    /**
     * Send filtered message notification to player (if enabled)
     * @param player The player to notify
     */
    public void notifyPlayer(Player player) {
        if (!chatConfig.playerFilteredMessage.isEmpty()) {
            player.sendMessage(TextUtils.deserializeString(chatConfig.playerFilteredMessage));
        }
    }

    /**
     * Reload filters (called when config is reloaded)
     */
    public void reload() {
        compileFilters();
        // Clear cooldowns on reload
        filterCooldowns.clear();
    }

    /**
     * Get the chat config (for command processor)
     */
    public ChatConfigManager getChatConfig() {
        return chatConfig;
    }

    /**
     * Result of a filter check
     */
    public static class FilterResult {
        private final boolean filtered;
        private final String filterName;
        private final String reason;
        private final String matchedText;
        private final Pattern matchedPattern;

        public FilterResult(boolean filtered, String filterName, String reason, String matchedText, Pattern matchedPattern) {
            this.filtered = filtered;
            this.filterName = filterName;
            this.reason = reason;
            this.matchedText = matchedText;
            this.matchedPattern = matchedPattern;
        }

        public boolean isFiltered() { return filtered; }
        public String getFilterName() { return filterName; }
        public String getReason() { return reason; }
        public String getMatchedText() { return matchedText; }
        public Pattern getMatchedPattern() { return matchedPattern; }
    }

}
