package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.managers.chat.ChatFilterManager;
import me.kermx.prismaUtils.managers.chat.ChatFilterService;
import me.kermx.prismaUtils.managers.chat.CommandChatProcessor;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatHandler implements Listener {

    private final PrismaUtils plugin;
    private final ChatFilterManager chatFilterManager;
    private final CommandChatProcessor commandProcessor;

    public ChatHandler(PrismaUtils plugin) {
        this.plugin = plugin;
        this.chatFilterManager = new ChatFilterManager(plugin, ConfigManager.getInstance().getChatConfig());
        this.commandProcessor = new CommandChatProcessor(plugin, chatFilterManager);

        plugin.getServer().getPluginManager().registerEvents(commandProcessor, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String originalMessage = event.getMessage();

        ChatFilterService.Decision decision = chatFilterManager.checkMessage(
                event.getPlayer(),
                originalMessage,
                ChatFilterService.Channel.PUBLIC_CHAT
        );

        if (decision.blocked()) {
            event.setCancelled(true);
        }

        if (decision.blocked() || decision.flagged()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                chatFilterManager.notifyStaff(event.getPlayer(), originalMessage, decision);
                if (decision.blocked()) {
                    chatFilterManager.notifyPlayer(event.getPlayer());
                }
            });
        }
    }

    public void reload() {
        chatFilterManager.reload();
    }

    public ChatFilterManager getChatFilterManager() {
        return chatFilterManager;
    }

    public CommandChatProcessor getCommandProcessor() {
        return commandProcessor;
    }
}