package me.kermx.prismaUtils.managers.playerdata;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager implements PlayerDataChangeListener {
    private static final int CURRENT_DATA_VERSION = 1;

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
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::saveAllDirtyData, 6000, 6000);

        // Schedule cache cleanup for offline players (every 10 minutes)
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::cleanupOfflinePlayerCache, 12000, 12000);
    }

    /**
     * This is the listener implementation that will be called whenever player data changes.
     */
    @Override
    public void onDataChanged(UUID playerId, String field, Object newValue) {
        // Mark the data as dirty when it changes
        markDataAsDirty(playerId);

        // Optionally log important changes for debugging
        if (plugin.getLogger().isLoggable(java.util.logging.Level.FINE)) {
            plugin.getLogger().fine(String.format("Player %s: %s changed to %s",
                    playerId, field, newValue));
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

    private PlayerData loadPlayerDataFromDisk(UUID playerId) {
        File file = new File(dataFolder, playerId.toString() + ".yml");

        if (!file.exists()) {
            // Return fresh PlayerData for new players
            plugin.getLogger().fine("Creating new player data for " + playerId);
            return new PlayerData.Builder(playerId).build();
        }

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            if (config.getKeys(false).isEmpty()) {
                plugin.getLogger().warning("Empty or corrupt player data for " + playerId + ", creating fresh data");
                backupCorruptFile(file);
                return new PlayerData.Builder(playerId).build();
            }

            // Check version
            int version = config.getInt("version", 1);
            if (version < CURRENT_DATA_VERSION) {
                plugin.getLogger().info("Migrating player data for " + playerId + " from version " + version);
                // Perform migration
            }

            PlayerData.Builder builder = new PlayerData.Builder(playerId)
                    .godEnabled(config.getBoolean("godMode", false))
                    .afkEnabled(config.getBoolean("afkEnabled", false))
                    .flightEnabled(config.getBoolean("flightEnabled", false))
                    .tempFlightSeconds(config.getLong("tempFlightSeconds", 0));

            String firstJoinStr = config.getString("firstJoin");
            if (firstJoinStr != null) {
                try {
                    builder.firstJoin(LocalDateTime.parse(firstJoinStr, formatter));
                } catch (Exception e) {
                    plugin.getLogger().warning("Could not parse firstJoin date for " + playerId + ": " + firstJoinStr);
                }
            }

            String tempFlightUpdatedStr = config.getString("tempFlightLastUpdated");
            if (tempFlightUpdatedStr != null) {
                try {
                    builder.tempFlightLastUpdated(LocalDateTime.parse(tempFlightUpdatedStr, formatter));
                } catch (Exception e) {
                    plugin.getLogger().warning("Could not parse tempFlightLastUpdated for " + playerId);
                }
            }

            if (config.contains("homes")) {
                Map<String, Home> homes = new HashMap<>();
                List<Map<?, ?>> homesList = config.getMapList("homes");
                for (Map<?, ?> homeMap : homesList) {
                    try {
                        String name = (String) homeMap.get("name");
                        String worldName = (String) homeMap.get("world");
                        double x = ((Number) homeMap.get("x")).doubleValue();
                        double y = ((Number) homeMap.get("y")).doubleValue();
                        double z = ((Number) homeMap.get("z")).doubleValue();
                        float yaw = ((Number) homeMap.get("yaw")).floatValue();
                        float pitch = ((Number) homeMap.get("pitch")).floatValue();

                        Home home = new Home(name, worldName, x, y, z, yaw, pitch);
                        homes.put(name.toLowerCase(), home);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Could not load home for " + playerId + ": " + e.getMessage());
                    }
                }
                builder.homes(homes);
            }

            if (config.contains("mailbox")) {
                List<MailMessage> mailMessages = new ArrayList<>();
                List<Map<?, ?>> mailList = config.getMapList("mailbox");
                for (Map<?, ?> mailMap : mailList) {
                    try {
                        UUID senderUUID = UUID.fromString((String) mailMap.get("sender"));
                        String senderName = (String) mailMap.get("senderName");
                        String message = (String) mailMap.get("message");

                        MailMessage mail = new MailMessage(senderUUID, senderName, message);
                        mailMessages.add(mail);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Could not load mail message for " + playerId + ": " + e.getMessage());
                    }
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
                } else {
                    plugin.getLogger().warning("World " + worldName + " not found for last location of " + playerId);
                }
            }

            return builder.build();

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load player data for " + playerId + ": " + e.getMessage());
            e.printStackTrace();
            backupCorruptFile(file);
            return new PlayerData.Builder(playerId).build();
        }
    }

    private void backupCorruptFile(File file) {
        try {
            File backup = new File(file.getParentFile(), file.getName() + ".corrupt." + System.currentTimeMillis());
            Files.copy(file.toPath(), backup.toPath());
            plugin.getLogger().info("Backed up corrupt file to: " + backup.getName());
        } catch (IOException e) {
            plugin.getLogger().warning("Could not backup corrupt file: " + e.getMessage());
        }
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
        File tempFile = new File(dataFolder, playerId.toString() + ".yml.tmp");

        YamlConfiguration config = new YamlConfiguration();

        config.set("version", CURRENT_DATA_VERSION);
        config.set("godMode", playerData.isGodEnabled());
        config.set("afkEnabled", playerData.isAfk());
        config.set("flightEnabled", playerData.isFlightEnabled());
        config.set("tempFlightSeconds", playerData.getTempFlightSeconds());

        if (playerData.getFirstJoin() != null) {
            config.set("firstJoin", playerData.getFirstJoin().format(formatter));
        }

        if (playerData.getTempFlightLastUpdated() != null) {
            config.set("tempFlightLastUpdated", playerData.getTempFlightLastUpdated().format(formatter));
        }

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
            config.save(tempFile);
            if (file.exists()) {
                file.delete();
            }
            if (!tempFile.renameTo(file)) {
                throw new IOException("Failed to rename temporary file to final file");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data for " + playerId);
            e.printStackTrace();

            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private void saveAllDirtyData() {
        Set<UUID> toSave;

        synchronized (dirtyData) {
            if (dirtyData.isEmpty()) {
                return;
            }
            toSave = new HashSet<>(dirtyData);
            dirtyData.clear();
        }

        int savedCount = 0;
        for (UUID playerId : toSave) {
            PlayerData data = playerDataCache.get(playerId);
            if (data != null) {
                savePlayerDataToDisk(playerId, data);
                savedCount++;
            }
        }
        if (savedCount > 0) {
            plugin.getLogger().fine("Auto-saved " + savedCount + " player data files");
        }
    }

    private void cleanupOfflinePlayerCache() {
        Set<UUID> toRemove = new HashSet<>();

        for (UUID playerId : playerDataCache.keySet()) {
            if (plugin.getServer().getPlayer(playerId) == null) {
                toRemove.add(playerId);
            }
        }
        for (UUID playerId : toRemove) {
            removePlayerData(playerId);
        }
        if (!toRemove.isEmpty()) {
            plugin.getLogger().fine("Cleaned up " + toRemove.size() + " offline player data from cache");
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
        plugin.getLogger().info("Saving all player data...");
        int count = 0;
        for (Map.Entry<UUID, PlayerData> entry : playerDataCache.entrySet()) {
            savePlayerDataToDisk(entry.getKey(), entry.getValue());
            count++;
        }
        dirtyData.clear();
        plugin.getLogger().info("Saved " + count + " player data files");
    }
}
