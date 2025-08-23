package me.kermx.prismaUtils.managers.chat;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.managers.general.configs.ChatConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandChatProcessor implements Listener {

    private final PrismaUtils plugin;
    private final ChatFilterManager chatFilterManager;
    private final EmojiManager emojiManager;
    private final ChatConfigManager chatConfig;

    public CommandChatProcessor(PrismaUtils plugin, ChatFilterManager chatFilterManager, EmojiManager emojiManager) {
        this.plugin = plugin;
        this.chatFilterManager = chatFilterManager;
        this.emojiManager = emojiManager;
        this.chatConfig = chatFilterManager.getChatConfig();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String fullCommand = event.getMessage();
        Player player = event.getPlayer();

        // Parse command
        String[] parts = fullCommand.substring(1).split(" ", 2); // Remove leading slash and split
        String commandName = parts[0].toLowerCase();

        // Check if this command should be monitored
        if (!chatConfig.monitoredCommands.contains(commandName)) {
            return;
        }

        // Extract message content based on command type
        String messageContent = extractMessageContent(commandName, parts);
        if (messageContent == null || messageContent.trim().isEmpty()) {
            return; // No message content to process
        }

        // Step 1: Check for chat filter violations FIRST on the original message content
        // This ensures we filter what the player actually typed (e.g., ":smile:")
        // rather than processed emojis (e.g., "😊")
        ChatFilterManager.FilterResult filterResult = chatFilterManager.checkMessage(player, messageContent);

        if (filterResult.isFiltered()) {
            // Cancel the command
            event.setCancelled(true);

            // Notify staff and player
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                chatFilterManager.notifyStaff(
                        player,
                        "Command: /" + commandName + " | " + messageContent,
                        filterResult.getFilterName(),
                        filterResult.getReason(),
                        filterResult.getMatchedText(),
                        filterResult.getMatchedPattern()
                );
                chatFilterManager.notifyPlayer(player);
            });

            return; // Stop processing if filtered
        }

        // Step 2: Process emojis ONLY after message passes all filters
        EmojiManager.ProcessResult emojiResult = emojiManager.processEmojis(player, messageContent);

        String processedMessage = messageContent;
        if (!emojiResult.isSuccess()) {
            // Notify about emoji permission but don't cancel command
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                emojiManager.notifyPermissionDenied(player, emojiResult.getDeniedEmoji());
            });
        } else {
            processedMessage = emojiResult.getProcessedMessage();
        }

        // If message was processed (emojis replaced), update the command
        if (!messageContent.equals(processedMessage)) {
            String newCommand = reconstructCommand(commandName, parts, processedMessage);
            event.setMessage(newCommand);
        }
    }

    /**
     * Extract message content from different command formats
     */
    private String extractMessageContent(String commandName, String[] parts) {
        if (parts.length < 2) {
            return null; // No message content
        }

        String fullArgs = parts[1];

        switch (commandName.toLowerCase()) {
            case "msg":
            case "tell":
            case "whisper":
            case "pm":
            case "message":
            case "vmessage":
            case "w":
            case "m":
                // Format: /msg <player> <message>
                String[] msgParts = fullArgs.split(" ", 2);
                return msgParts.length >= 2 ? msgParts[1] : null;

            case "reply":
            case "r":
            case "vreply":
                // Format: /reply <message>
                return fullArgs;

            case "me":
            case "vme":
                // Format: /me <message>
                return fullArgs;

            case "say":
                // Format: /say <message>
                return fullArgs;

            case "lc":
            case "local":
            case "tc":
            case "nc":
            case "g":
            case "global":
            case "net":
            case "network":
            case "tr":
            case "trade":
                // Format: /<channel> <message>
                return fullArgs;

            default:
                return fullArgs; // Default behavior
        }
    }

    /**
     * Reconstruct command with processed message content
     */
    private String reconstructCommand(String commandName, String[] parts, String processedMessage) {
        if (parts.length < 2) {
            return "/" + commandName; // Shouldn't happen, but safety check
        }

        String fullArgs = parts[1];

        switch (commandName.toLowerCase()) {
            case "msg":
            case "tell":
            case "whisper":
            case "pm":
            case "message":
            case "vmessage":
            case "w":
            case "m":
                // Format: /msg <player> <message>
                String[] msgParts = fullArgs.split(" ", 2);
                if (msgParts.length >= 2) {
                    return "/" + commandName + " " + msgParts[0] + " " + processedMessage;
                }
                break;

            case "reply":
            case "r":
            case "vreply":
            case "me":
            case "vme":
            case "say":
            case "lc":
            case "local":
            case "tc":
            case "nc":
            case "g":
            case "global":
            case "net":
            case "network":
            case "tr":
            case "trade":
                // Direct message replacement
                return "/" + commandName + " " + processedMessage;
        }

        // Default fallback
        return "/" + commandName + " " + processedMessage;
    }
}
