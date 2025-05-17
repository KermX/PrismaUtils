package me.kermx.prismaUtils.managers.features;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.integrations.SitService;
import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AfkManager implements Listener {
    private final PrismaUtils plugin;
    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private final Map<UUID, Boolean> afkStatus = new HashMap<>();
    private final Map<UUID, Boolean> teleportedToAfk = new HashMap<>();
    private final Map<UUID, BukkitTask> teleportTasks = new HashMap<>();

    private final long afkThreshold;
    private final long teleportThreshold;
    private final Location afkLocation;

    private final String afkMessage;
    private final String returnMessage;

    public AfkManager(PrismaUtils plugin) {
        this.plugin = plugin;

        // Load configuration
        afkThreshold = ConfigManager.getInstance().getAfkConfig().afkThresholdSeconds* 1000L;
        teleportThreshold = ConfigManager.getInstance().getAfkConfig().afkTeleportAfterSeconds * 1000L;
        afkLocation = ConfigManager.getInstance().getAfkConfig().afkLocation;
        afkMessage = ConfigManager.getInstance().getAfkConfig().afkMessage;
        returnMessage = ConfigManager.getInstance().getAfkConfig().afkReturnMessage;

        // Start the checking task
        startAfkChecker();

        // Load saved AFK data for any online players
        loadPersistentData();
    }

    private void loadPersistentData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(playerId);
            if (playerData.isAfk()) {
                afkStatus.put(playerId, true);

                if (playerData.getLastLocation() != null) {
                    teleportedToAfk.put(playerId, true);
                }
            }

            lastActivity.put(playerId, System.currentTimeMillis());
        }
    }

    private void startAfkChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("prismautils.afk")) continue;

                    UUID uuid = player.getUniqueId();
                    if (!lastActivity.containsKey(uuid)) {
                        lastActivity.put(uuid, currentTime);
                        continue;
                    }

                    long lastActiveTime = lastActivity.get(uuid);
                    long timeSinceActive = currentTime - lastActiveTime;

                    // Check if player should be marked as AFK
                    if (!afkStatus.getOrDefault(uuid, false) && timeSinceActive >= afkThreshold) {
                        setAfk(player, true);
                    }

                    // Check if player should be teleported
                    if (afkStatus.getOrDefault(uuid, false) && timeSinceActive >= (afkThreshold + teleportThreshold)
                            && !teleportedToAfk.getOrDefault(uuid, false)) {
                        teleportToAfkLocation(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only consider significant movement (not just head rotation)
        if (!event.hasChangedOrientation()) {
            return;
        }
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        // Don't count /afk command as activity
        if (!event.getMessage().toLowerCase().startsWith("afk")) {
            updateActivity(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Get the player data to check if they were AFK when the server stopped
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);

        if (playerData.isAfk()) {
            // If they were AFK, maintain their status
            afkStatus.put(uuid, true);

            // If they have a saved location, they were teleported to AFK location
            if (playerData.getLastLocation() != null) {
                teleportedToAfk.put(uuid, true);
            }
        } else {
            // Otherwise, mark them as active
            afkStatus.put(uuid, false);
            teleportedToAfk.put(uuid, false);
        }

        lastActivity.put(uuid, System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        // Save AFK status to player data before removing from maps
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
        boolean isAfk = afkStatus.getOrDefault(uuid, false);
        playerData.setAfk(isAfk);

        // lastLocation is already handled by the LastLocationHandler
        // or will be saved if they were teleported to AFK location

        lastActivity.remove(uuid);
        afkStatus.remove(uuid);
        teleportedToAfk.remove(uuid);

        BukkitTask task = teleportTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }

        // Make sure data is saved
        plugin.getPlayerDataManager().markDataAsDirty(uuid);
    }

    public void updateActivity(Player player) {
        UUID uuid = player.getUniqueId();
        lastActivity.put(uuid, System.currentTimeMillis());

        // If player was AFK, unmark them
        if (afkStatus.getOrDefault(uuid, false)) {
            setAfk(player, false);
        }
    }

    public void setAfk(Player player, boolean afk) {
        UUID uuid = player.getUniqueId();

        if (afkStatus.getOrDefault(uuid, false) == afk) {return;}

        afkStatus.put(uuid, afk);

        if (afk) {
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
            playerData.setAfk(true);

            Bukkit.broadcast(TextUtils.deserializeString(afkMessage, Placeholder.component("player", player.displayName())));

            teleportTasks.put(uuid, new BukkitRunnable() {
                @Override
                public void run() {
                    if (afkStatus.getOrDefault(uuid, false)) {
                        teleportToAfkLocation(player);
                    }
                }
            }.runTaskLater(plugin, teleportThreshold / 50)); // Convert ms to ticks

        } else {
            // Player is no longer AFK
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
            playerData.setAfk(false);

            // Broadcast return message
            Bukkit.broadcast(TextUtils.deserializeString(returnMessage, Placeholder.component("player", player.displayName())));

            // Cancel teleport task if it exists
            BukkitTask task = teleportTasks.remove(uuid);
            if (task != null) {
                task.cancel();
            }

            // Return from AFK location if applicable
            if (teleportedToAfk.getOrDefault(uuid, false)) {
                returnFromAfkLocation(player);
                teleportedToAfk.put(uuid, false);
            }
        }

        // Mark player data as dirty to ensure persistence
        plugin.getPlayerDataManager().markDataAsDirty(uuid);
    }

    private void teleportToAfkLocation(Player player) {
        UUID uuid = player.getUniqueId();

        if (teleportedToAfk.getOrDefault(uuid, false)) {
            // Already teleported
            return;
        }

        // Store current location in PlayerData
        Location currentLocation = player.getLocation().clone();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
        playerData.setLastLocation(currentLocation);

        // Explicitly save the player data to ensure persistence
        plugin.getPlayerDataManager().markDataAsDirty(uuid);
        plugin.getPlayerDataManager().savePlayerData(uuid);

        // Check if player is sitting
        SitService sitService = plugin.getSitService();
        boolean wasSitting = sitService != null && sitService.isGSitAvailable() && sitService.isPlayerSitting(player);

        if (wasSitting) {
            // Force them to stand up
            sitService.standPlayer(player, true);

            // Wait a moment to ensure they're fully stood up, then teleport
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Make sure the player is still online and AFK
                if (player.isOnline() && afkStatus.getOrDefault(uuid, false)) {
                    player.teleportAsync(afkLocation).thenAccept(success -> {
                        if (success) {
                            // Set the flag after teleportation is complete
                            teleportedToAfk.put(uuid, true);
                        }
                    });
                }
            }, 5L); // Wait 5 ticks (1/4 second) before teleporting
        } else {
            // Player is not sitting, teleport immediately
            player.teleportAsync(afkLocation).thenAccept(success -> {
                if (success) {
                    // Set the flag after teleportation is complete
                    teleportedToAfk.put(uuid, true);
                }
            });
        }
    }

    private void returnFromAfkLocation(Player player) {
        UUID uuid = player.getUniqueId();

        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
        Location previousLocation = playerData.getLastLocation();

        if (previousLocation != null) {
            player.teleportAsync(previousLocation);

            // Clear the saved location in player data (or keep it for /back command)
            // playerData.setLastLocation(null);
            // plugin.getPlayerDataManager().markDataAsDirty(uuid);
        }
    }

    public boolean isAfk(UUID uuid) {
        return afkStatus.getOrDefault(uuid, false);
    }
}


