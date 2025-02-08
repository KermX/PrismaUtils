package me.kermx.prismaUtils.managers.general.configs;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MessagesConfigManager {
    private final PrismaUtils plugin;
    private FileConfiguration messages;
    private File messagesFile;

    // General Messages
    public String noPermissionMessage;
    public String mustSpecifyPlayerMessage;
    public String playerNotFoundMessage;
    public String incorrectUsageMessage;

    // Seen Messages
    public String seenOfflineMessage;
    public String seenNeverJoinedMessage;
    public String seenOnlineMessage;

    // Spawner Messages
    public String spawnerNoSilkWarningMessage;
    public String spawnerName;

    // Feed Messages
    public String feedMessage;
    public String feedAllMessage;
    public String feedOtherMessage;
    public String feedFedByOtherMessage;

    // Fly Speed Messages
    public String flyspeedResetMessage;
    public String flyspeedSetMessage;
    public String flyspeedInvalidSpeedMessage;

    // Heal Messages
    public String healMessage;
    public String healAllMessage;
    public String healOtherMessage;
    public String healHealedByOtherMessage;

    // Item Name Messages
    public String itemNameMessage;
    public String itemNameInvalidItemMessage;

    // Near Messages
    public String nearNoPlayersMessage;
    public String nearNearPlayersMessage;
    public String nearNearbyPlayersMessage;
    public String nearInvalidRadiusMessage;

    // ping Messages
    public String pingMessage;
    public String pingOtherMessage;

    // pTime Messages
    public String pTimeResetMessage;
    public String pTimeSetMessage;
    public String pTimeInvalidTimeMessage;

    // pWeather Messages
    public String pWeatherResetMessage;
    public String pWeatherSetMessage;
    public String pWeatherInvalidWeatherMessage;

    // Repair Messages
    public String repairNoItemInHandMessage;
    public String repairInvalidItemMessage;
    public String repairRepairedMessage;
    public String repairAllRepairedMessage;

    // Top Bottom Messages
    public String topMessage;
    public String bottomMessage;
    public String bottomMessageInvalidBlock;

    // God Messages
    public String godEnabledMessage;
    public String godDisabledMessage;

    // AFK Title Messages
    public String afkPlaceholder;
    public List<String> afkTitles;
    public List<String> afkSubtitles;

    public MessagesConfigManager(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    public void loadConfig(){
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream == null) {
            plugin.getLogger().warning("Default messages.yml not found in the JAR. No defaults to merge!");
        } else {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            messages.setDefaults(defaultConfig);
            messages.options().copyDefaults(true);

            try {
                messages.save(messagesFile);
                plugin.getLogger().info("Merged any missing keys into messages.yml (if needed).");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save merged messages.yml!");
                e.printStackTrace();
            }
        }

        // General Messages
        noPermissionMessage = messages.getString("general_messages.no_permission");
        mustSpecifyPlayerMessage = messages.getString("general_messages.must_specify_player");
        playerNotFoundMessage = messages.getString("general_messages.player_not_found");
        incorrectUsageMessage = messages.getString("general_messages.incorrect_usage");

        // Seen Messages
        seenOfflineMessage = messages.getString("seen_messages.seen_offline_message");
        seenNeverJoinedMessage = messages.getString("seen_messages.seen_never_joined_message");
        seenOnlineMessage = messages.getString("seen_messages.seen_online_message");

        // Spawner Messages
        spawnerNoSilkWarningMessage = messages.getString("spawner_messages.no_silk_warning_message");
        spawnerName = messages.getString("spawner_messages.spawner_name");

        // Feed Messages
        feedMessage = messages.getString("feed_messages.feed_message");
        feedAllMessage = messages.getString("feed_messages.feed_all_message");
        feedOtherMessage = messages.getString("feed_messages.feed_other_message");
        feedFedByOtherMessage = messages.getString("feed_messages.feed_fed_by_other_message");

        // Fly Speed Messages
        flyspeedResetMessage = messages.getString("flyspeed_messages.flyspeed_reset_message");
        flyspeedSetMessage = messages.getString("flyspeed_messages.flyspeed_set_message");
        flyspeedInvalidSpeedMessage = messages.getString("flyspeed_messages.flyspeed_invalid_speed_message");

        // Heal Messages
        healMessage = messages.getString("heal_messages.heal_message");
        healAllMessage = messages.getString("heal_messages.heal_all_message");
        healOtherMessage = messages.getString("heal_messages.heal_other_message");
        healHealedByOtherMessage = messages.getString("heal_messages.heal_healed_by_other_message");

        // Item Name Messages
        itemNameMessage = messages.getString("itemname_messages.itemname_message");
        itemNameInvalidItemMessage = messages.getString("itemname_messages.itemname_no_item_message");

        // Near Messages
        nearNoPlayersMessage = messages.getString("near_messages.near_no_players_message");
        nearNearPlayersMessage = messages.getString("near_messages.near_near_players_message");
        nearNearbyPlayersMessage = messages.getString("near_messages.near_nearby_players_message");
        nearInvalidRadiusMessage = messages.getString("near_messages.near_invalid_radius_message");

        // ping Messages
        pingMessage = messages.getString("ping_messages.ping_message");
        pingOtherMessage = messages.getString("ping_messages.ping_other_message");

        // pTime Messages
        pTimeResetMessage = messages.getString("ptime_messages.ptime_reset_message");
        pTimeSetMessage = messages.getString("ptime_messages.ptime_set_message");
        pTimeInvalidTimeMessage = messages.getString("ptime_messages.ptime_invalid_time_message");

        // pWeather Messages
        pWeatherResetMessage = messages.getString("pweather_messages.pweather_reset_message");
        pWeatherSetMessage = messages.getString("pweather_messages.pweather_set_message");
        pWeatherInvalidWeatherMessage = messages.getString("pweather_messages.pweather_invalid_weather_message");

        // Repair Messages
        repairNoItemInHandMessage = messages.getString("repair_messages.repair_no_item_message");
        repairInvalidItemMessage = messages.getString("repair_messages.repair_invalid_item_message");
        repairRepairedMessage = messages.getString("repair_messages.repair_message");
        repairAllRepairedMessage = messages.getString("repair_messages.repair_all_message");

        // Top Bottom Messages
        topMessage = messages.getString("top_messages.top_message");
        bottomMessage = messages.getString("bottom_messages.bottom_message");
        bottomMessageInvalidBlock = messages.getString("bottom_messages.bottom_message_invalid_block");

        // God Messages
        godEnabledMessage = messages.getString("god_messages.god_enabled_message");
        godDisabledMessage = messages.getString("god_messages.god_disabled_message");

        // AFK Title Messages
        afkPlaceholder = messages.getString("afk_title_feature.afk_placeholder");
        afkTitles = messages.getStringList("afk_title_feature.afk_titles");
        afkSubtitles = messages.getStringList("afk_title_feature.afk_subtitles");
    }

    public void reload() {
        loadConfig();
    }

    public void save() {
        try {
            messages.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save messages.yml!");
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return messages;
    }
}
