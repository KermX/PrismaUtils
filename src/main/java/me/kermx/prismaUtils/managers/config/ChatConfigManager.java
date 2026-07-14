package me.kermx.prismaUtils.managers.config;

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
        if (defaultStream != null) {
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
}