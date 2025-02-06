package me.kermx.prismaUtils.managers.general;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.managers.general.configs.DeathMessagesConfigManager;
import me.kermx.prismaUtils.managers.general.configs.MainConfigManager;
import me.kermx.prismaUtils.managers.general.configs.MessagesConfigManager;

import java.util.List;

public class ConfigManager {

    private static ConfigManager instance;
    private final PrismaUtils plugin;

    private MainConfigManager mainConfigManager;
    private MessagesConfigManager messagesConfigManager;
    private DeathMessagesConfigManager deathMessagesConfigManager;

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

        mainConfigManager.loadConfig();
        messagesConfigManager.loadConfig();
        deathMessagesConfigManager.loadConfig();
    }

    public void reloadAll() {
        mainConfigManager.reload();
        messagesConfigManager.reload();
        deathMessagesConfigManager.reload();
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
}
