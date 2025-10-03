package me.kermx.prismaUtils.managers.general;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.managers.general.configs.*;

public class ConfigManager {

    private static ConfigManager instance;
    private final PrismaUtils plugin;

    private MainConfigManager mainConfigManager;
    private MessagesConfigManager messagesConfigManager;
    private DeathMessagesConfigManager deathMessagesConfigManager;
    private WarpsConfigManager warpsConfigManager;
    private AfkConfigManager afkConfigManager;
    private ChatConfigManager chatConfigManager;
    private EventPlaceholderConfigManager eventPlaceholderConfigManager;

    private ConfigManager(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    public static void initialize(PrismaUtils plugin) {
        if (instance == null) {
            instance = new ConfigManager(plugin);
            instance.loadAll();
        }
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConfigManager is not initialized!");
        }
        return instance;
    }

    private void loadAll() {
        mainConfigManager = new MainConfigManager(plugin);
        messagesConfigManager = new MessagesConfigManager(plugin);
        deathMessagesConfigManager = new DeathMessagesConfigManager(plugin);
        warpsConfigManager = new WarpsConfigManager(plugin);
        afkConfigManager = new AfkConfigManager(plugin);
        chatConfigManager = new ChatConfigManager(plugin);
        eventPlaceholderConfigManager = new EventPlaceholderConfigManager(plugin);

        mainConfigManager.loadConfig();
        messagesConfigManager.loadConfig();
        deathMessagesConfigManager.loadConfig();
        warpsConfigManager.loadConfig();
        afkConfigManager.loadConfig();
        chatConfigManager.loadConfig();
        eventPlaceholderConfigManager.loadConfig();
    }

    public void reloadAll() {
        mainConfigManager.reload();
        messagesConfigManager.reload();
        deathMessagesConfigManager.reload();
        warpsConfigManager.reload();
        afkConfigManager.reload();
        chatConfigManager.reload();
        eventPlaceholderConfigManager.reload();
    }

    public MainConfigManager getMainConfig() {
        return mainConfigManager;
    }

    public MessagesConfigManager getMessagesConfig() {
        return messagesConfigManager;
    }

    public DeathMessagesConfigManager getDeathMessagesConfig() {
        return deathMessagesConfigManager;
    }

    public WarpsConfigManager getWarpsConfig() {return warpsConfigManager;}

    public AfkConfigManager getAfkConfig() {return afkConfigManager;}

    public ChatConfigManager getChatConfig() {return chatConfigManager;}

    public EventPlaceholderConfigManager getEventPlaceholdersConfig() {return eventPlaceholderConfigManager;}
}
