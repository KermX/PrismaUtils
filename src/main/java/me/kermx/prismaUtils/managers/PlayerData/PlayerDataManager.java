package me.kermx.prismaUtils.managers.PlayerData;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager implements PlayerDataChangeListener {
    private final PrismaUtils plugin;
    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();
    private final Set<UUID> dirtyData = Collections.synchronizedSet(new HashSet<>());
    private final File dataFolder;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public PlayerDataManager(PrismaUtils plugin) {
        this.plugin = plugin;
        dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // Schedule periodic saves for dirty data
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::saveAllDirtyData, 6000, 6000); // Every 5 minutes
    }

    /**
     * This is the listener implementation that will be called whenever player data changes.
     */
    @Override
    public void onDataChanged(UUID playerId, String field, Object newValue) {
        // Mark the data as dirty when it changes
        markDataAsDirty(playerId);

        // Optionally log important changes for debugging
        plugin.getLogger().fine("Player data changed for " + playerId + ": " + field + " = " + newValue);

        // You could also add special handling for specific fields if needed
        if ("godMode".equals(field)) {
            plugin.getLogger().info("Player " + playerId + " god mode changed to " + newValue);
        }
    }

    public PlayerData getPlayerData(UUID playerId) {
        PlayerData data = playerDataCache.get(playerId);
        if (data == null) {
            // Lazy loading - only load if not in cache
            data = loadPlayerDataFromDisk(playerId);

            // Register this manager as a listener for data changes
            data.addChangeListener(this);

            playerDataCache.put(playerId, data);
        }
        return data;
    }

    // Rest of the class remains the same
    private PlayerData loadPlayerDataFromDisk(UUID playerId) {
        File file = new File(dataFolder, playerId.toString() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        PlayerData.Builder builder = new PlayerData.Builder(playerId)
                .flyEnabled(config.getBoolean("flyEnabled", false))
                .godEnabled(config.getBoolean("godMode", false))
                .afkEnabled(config.getBoolean("afkEnabled", false));

        String firstJoinStr = config.getString("firstJoin");
        if (firstJoinStr != null) {
            builder.firstJoin(LocalDateTime.parse(firstJoinStr, formatter));
        }

        if (config.contains("homes")) {
            Map<String, Home> homes = new HashMap<>();
            List<Map<?, ?>> homesList = config.getMapList("homes");
            for (Map<?, ?> homeMap : homesList) {
                String name = (String) homeMap.get("name");
                String worldName = (String) homeMap.get("world");
                double x = ((Number) homeMap.get("x")).doubleValue();
                double y = ((Number) homeMap.get("y")).doubleValue();
                double z = ((Number) homeMap.get("z")).doubleValue();
                float yaw = ((Number) homeMap.get("yaw")).floatValue();
                float pitch = ((Number) homeMap.get("pitch")).floatValue();

                Home home = new Home(name, worldName, x, y, z, yaw, pitch);
                homes.put(name.toLowerCase(), home);
            }
            builder.homes(homes);
        }

        if (config.contains("mailbox")) {
            List<MailMessage> mailMessages = new ArrayList<>();
            List<Map<?, ?>> mailList = config.getMapList("mailbox");
            for (Map<?, ?> mailMap : mailList) {
                UUID senderUUID = UUID.fromString((String) mailMap.get("sender"));
                String senderName = (String) mailMap.get("senderName");
                String message = (String) mailMap.get("message");
                String timestampStr = (String) mailMap.get("timestamp");

                MailMessage mail = new MailMessage(senderUUID, senderName, message);
                mailMessages.add(mail);
            }
            builder.mailbox(mailMessages);
        }

        // Load last location if exists
        if (config.contains("lastLocation.world")) {
            String worldName = config.getString("lastLocation.world");
            World world = Bukkit.getWorld(worldName);

            if (world != null) {
                double x = config.getDouble("lastLocation.x");
                double y = config.getDouble("lastLocation.y");
                double z = config.getDouble("lastLocation.z");
                float yaw = (float) config.getDouble("lastLocation.yaw");
                float pitch = (float) config.getDouble("lastLocation.pitch");

                Location lastLocation = new Location(world, x, y, z, yaw, pitch);
                builder.lastLocation(lastLocation);
            }
        }

        return builder.build();
    }

    public void markDataAsDirty(UUID playerId) {
        dirtyData.add(playerId);
    }

    public void savePlayerData(UUID playerId) {
        PlayerData playerData = playerDataCache.get(playerId);
        if (playerData == null) {
            return;
        }

        savePlayerDataToDisk(playerId, playerData);
        dirtyData.remove(playerId);
    }

    private void savePlayerDataToDisk(UUID playerId, PlayerData playerData) {
        File file = new File(dataFolder, playerId.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("flyEnabled", playerData.isFlyEnabled());
        config.set("godMode", playerData.isGodEnabled());
        config.set("afkEnabled", playerData.isAfk());
        config.set("firstJoin", playerData.getFirstJoin().format(formatter));

        List<Map<String, Object>> homesList = new ArrayList<>();
        for (Home home : playerData.getHomes().values()) {
            homesList.add(home.toMap());
        }
        config.set("homes", homesList);

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

        // Save last location
        Location lastLocation = playerData.getLastLocation();
        if (lastLocation != null) {
            config.set("lastLocation.world", lastLocation.getWorld().getName());
            config.set("lastLocation.x", lastLocation.getX());
            config.set("lastLocation.y", lastLocation.getY());
            config.set("lastLocation.z", lastLocation.getZ());
            config.set("lastLocation.yaw", lastLocation.getYaw());
            config.set("lastLocation.pitch", lastLocation.getPitch());
        }


        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data for " + playerId);
            e.printStackTrace();
        }
    }

    private void saveAllDirtyData() {
        synchronized (dirtyData) {
            for (UUID playerId : dirtyData) {
                PlayerData data = playerDataCache.get(playerId);
                if (data != null) {
                    savePlayerDataToDisk(playerId, data);
                }
            }
            dirtyData.clear();
        }
    }

    public void removePlayerData(UUID playerId) {
        PlayerData data = playerDataCache.get(playerId);
        if (data != null) {
            // Remove listener before removing from cache
            data.removeChangeListener(this);

            // Save before removing from cache if it's dirty
            if (dirtyData.contains(playerId)) {
                savePlayerData(playerId);
            }
        }
        playerDataCache.remove(playerId);
    }

    // Call this on plugin disable to save all data
    public void saveAllData() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataCache.entrySet()) {
            savePlayerDataToDisk(entry.getKey(), entry.getValue());
        }
        dirtyData.clear();
    }
}
