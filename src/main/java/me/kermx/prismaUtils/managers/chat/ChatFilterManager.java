package me.kermx.prismaUtils.managers.chat;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.managers.config.ChatConfigManager;
import org.bukkit.entity.Player;


public class ChatFilterManager {

    private final PrismaUtils plugin;
    private final ChatConfigManager chatConfig;
    private ChatFilterConfig config;
    private ChatFilterService service;

    public ChatFilterManager(PrismaUtils plugin, ChatConfigManager chatConfig) {
        this.plugin = plugin;
        this.chatConfig = chatConfig;
        reload();
    }

    public void reload() {
        this.config = ChatFilterConfig.loadFromChatConfig(plugin, chatConfig);
        this.service = new ChatFilterService(plugin, config);
    }

    public ChatFilterConfig config() {
        return config;
    }

    public ChatFilterService.Decision checkMessage(Player player, String message, ChatFilterService.Channel channel) {
        if (service == null) {
            reload();
        }
        return service.check(player, message, channel);
    }

    public void notifyStaff(Player player, String originalMessage, ChatFilterService.Decision decision) {
        if (service != null) {
            service.notifyStaff(player, originalMessage, decision);
        }
    }

    public void notifyPlayer(Player player) {
        if (service != null) {
            service.notifyPlayerBlocked(player);
        }
    }

    public ChatConfigManager getChatConfig() {
        return chatConfig;
    }
}