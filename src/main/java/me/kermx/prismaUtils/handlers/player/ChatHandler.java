package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.managers.chat.ChatFilterManager;
import me.kermx.prismaUtils.managers.chat.CommandChatProcessor;
import me.kermx.prismaUtils.managers.chat.EmojiManager;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatHandler implements Listener {

    private final PrismaUtils plugin;
    private final ChatFilterManager chatFilterManager;
    private final EmojiManager emojiManager;
    private final CommandChatProcessor commandProcessor;

    public ChatHandler(PrismaUtils plugin) {
        this.plugin = plugin;
        this.chatFilterManager = new ChatFilterManager(plugin, ConfigManager.getInstance().getChatConfig());
        this.emojiManager = new EmojiManager(plugin, ConfigManager.getInstance().getChatConfig());
        this.commandProcessor = new CommandChatProcessor(plugin, chatFilterManager, emojiManager);

        // Register command processor as event listener
        plugin.getServer().getPluginManager().registerEvents(commandProcessor, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String originalMessage = event.getMessage();

        // Step 1: Check for chat filter violations FIRST
        ChatFilterManager.FilterResult filterResult = chatFilterManager.checkMessage(event.getPlayer(), originalMessage);

        if (filterResult.isFiltered()) {
            // Cancel the message
            event.setCancelled(true);

            // Notify staff (async-safe)
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                chatFilterManager.notifyStaff(
                        event.getPlayer(),
                        originalMessage,
                        filterResult.getFilterName(),
                        filterResult.getReason(),
                        filterResult.getMatchedText(),
                        filterResult.getMatchedPattern()
                );
                chatFilterManager.notifyPlayer(event.getPlayer());
            });

            return; // Stop processing if filtered
        }

        // Step 2: Process emojis ONLY if message passed filter
        EmojiManager.ProcessResult emojiResult = emojiManager.processEmojis(event.getPlayer(), originalMessage);

        if (!emojiResult.isSuccess()) {
            // Notify about emoji permission but allow original message
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                emojiManager.notifyPermissionDenied(event.getPlayer(), emojiResult.getDeniedEmoji());
            });
            // Use original message
            event.setMessage(originalMessage);
        } else {
            // Use processed message with emojis
            event.setMessage(emojiResult.getProcessedMessage());
        }
    }

    /**
     * Reload managers when config is reloaded
     */
    public void reload() {
        chatFilterManager.reload();
        emojiManager.reload();
    }

    // Getters for potential future use
    public ChatFilterManager getChatFilterManager() {
        return chatFilterManager;
    }

    public EmojiManager getEmojiManager() {
        return emojiManager;
    }

    public CommandChatProcessor getCommandProcessor() {
        return commandProcessor;
    }
}

