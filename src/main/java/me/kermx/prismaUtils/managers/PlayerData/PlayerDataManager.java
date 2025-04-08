package me.kermx.prismaUtils.managers.PlayerData;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PlayerDataManager {
    private final PrismaUtils plugin;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final File dataFolder;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public PlayerDataManager(PrismaUtils plugin) {
        this.plugin = plugin;
        dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public PlayerData getPlayerData(UUID playerId) {
        return playerDataMap.get(playerId);
    }

    public void loadPlayerData(UUID playerId) {
        File file = new File(dataFolder, playerId.toString() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        PlayerData playerData = new PlayerData(playerId);
        playerData.setFlyEnabled(config.getBoolean("flyEnabled", false));
        playerData.setGodEnabled(config.getBoolean("godMode", false));
        String firstJoinStr = config.getString("firstJoin");

        if (firstJoinStr != null) {
            playerData.setFirstJoin(LocalDateTime.parse(firstJoinStr, formatter));
        }

        if (config.contains("mailbox")) {
            List<Map<?, ?>> mailList = config.getMapList("mailbox");
            for (Map<?, ?> mailMap : mailList) {
                UUID senderUUID = UUID.fromString((String) mailMap.get("sender"));
                String senderName = (String) mailMap.get("senderName");
                String message = (String) mailMap.get("message");
                String timestampStr = (String) mailMap.get("timestamp");

                MailMessage mail = new MailMessage(senderUUID, senderName, message);
                playerData.addMailMessage(mail);
            }
        }

        playerDataMap.put(playerId, playerData);
    }

    public void savePlayerData(UUID playerId) {
        PlayerData playerData = playerDataMap.get(playerId);
        if (playerData == null) {
            return;
        }
        File file = new File(dataFolder, playerId.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("flyEnabled", playerData.isFlyEnabled());
        config.set("godMode", playerData.isGodEnabled());
        config.set("firstJoin", playerData.getFirstJoin().format(formatter));

        // Save mail messages
        List<Map<String, Object>> mailList = new ArrayList<>();
        for (MailMessage mail : playerData.getMailbox()) {
            Map<String, Object> mailMap = new HashMap<>();
            mailMap.put("sender", mail.getSender().toString());
            mailMap.put("senderName", mail.getSenderName());
            mailMap.put("message", mail.getMessage());
            mailMap.put("timestamp", mail.getTimestamp().format(formatter));
            mailList.add(mailMap);
        }
        config.set("mailbox", mailList);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data for " + playerId);
            e.printStackTrace();
        }
    }

    public void removePlayerData(UUID playerId) {
        playerDataMap.remove(playerId);
    }

}

