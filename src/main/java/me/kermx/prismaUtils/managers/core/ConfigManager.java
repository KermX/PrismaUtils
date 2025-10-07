package me.kermx.prismaUtils.managers.core;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.managers.config.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public enum ConfigType {
        MAIN("config", "Main configuration file"),
        MESSAGES("messages", "Messages configuration file"),
        DEATH_MESSAGES("death_messages", "Death messages configuration file"),
        WARPS("warps", "Warps configuration file"),
        AFK("afk", "AFK system configuration file"),
        CHAT("chat", "Chat system configuration file"),
        EVENTS("events", "Event placeholders configuration file"),
        FEATURES("features", "Feature toggles configuration file");

        private final String name;
        private final String description;

        ConfigType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public static ConfigType fromName(String name) {
            for (ConfigType type : values()) {
                if (type.name.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }

        public static List<String> getNames() {
            List<String> names = new ArrayList<>();
            for (ConfigType type : values()) {
                names.add(type.name);
            }
            return names;
        }
    }

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

    public boolean reload(ConfigType type) {
        try {
            switch (type) {
                case MAIN:
                    mainConfigManager.reload();
                    break;
                case MESSAGES:
                    messagesConfigManager.reload();
                    break;
                case DEATH_MESSAGES:
                    deathMessagesConfigManager.reload();
                    break;
                case WARPS:
                    warpsConfigManager.reload();
                    break;
                case AFK:
                    afkConfigManager.reload();
                    break;
                case CHAT:
                    chatConfigManager.reload();
                    if (plugin.getChatHandler() != null) {
                        plugin.getChatHandler().reload();
                    }
                    break;
                case EVENTS:
                    eventPlaceholderConfigManager.reload();
                    break;
                case FEATURES:
                    plugin.getFeatureToggleManager().reload();
                    break;
                default:
                    return false;
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error while reloading config " + type.getName() + " configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void reloadAll() {
        mainConfigManager.reload();
        messagesConfigManager.reload();
        deathMessagesConfigManager.reload();
        warpsConfigManager.reload();
        afkConfigManager.reload();
        chatConfigManager.reload();
        eventPlaceholderConfigManager.reload();

        if (plugin.getChatHandler() != null) {
            plugin.getChatHandler().reload();
        }
    }

    public Map<ConfigType, String> getConfigTypes() {
        Map<ConfigType, String> types = new LinkedHashMap<>();
        for (ConfigType type : ConfigType.values()) {
            types.put(type, type.getDescription());
        }
        return types;
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
