package me.kermx.prismaUtils.managers.features;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.integrations.SitService;
import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.BlockUtils;
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
    private final Map<UUID, BukkitTask> warningTasks = new HashMap<>();

    private final long afkThreshold;
    private final long teleportThreshold;
    private final Location afkLocation;

    private final String afkMessage;
    private final String returnMessage;
    private final String teleportWarningMessage;


    public AfkManager(PrismaUtils plugin) {
        this.plugin = plugin;

        // Load configuration
        afkThreshold = ConfigManager.getInstance().getAfkConfig().afkThresholdSeconds* 1000L;
        teleportThreshold = ConfigManager.getInstance().getAfkConfig().afkTeleportAfterSeconds * 1000L;
        afkLocation = ConfigManager.getInstance().getAfkConfig().afkLocation;
        afkMessage = ConfigManager.getInstance().getAfkConfig().afkMessage;
        returnMessage = ConfigManager.getInstance().getAfkConfig().afkReturnMessage;
        teleportWarningMessage = "<gold>You will be teleported to the AFK area in</gold> <red><time></red> <gold>seconds!</gold>";


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
            } else {
                afkStatus.put(playerId, false);
                teleportedToAfk.put(playerId, false);
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
                    if (player.isDead()) continue;

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

                    // Check if we should warn player about upcoming teleport
                    if (afkStatus.getOrDefault(uuid, false) &&
                            !teleportedToAfk.getOrDefault(uuid, false) &&
                            timeSinceActive >= (afkThreshold + teleportThreshold * 0.5) &&
                            timeSinceActive < (afkThreshold + teleportThreshold)) {

                        // Send a warning if one isn't already scheduled
                        if (!warningTasks.containsKey(uuid)) {
                            long remainingSeconds = (afkThreshold + teleportThreshold - timeSinceActive) / 1000;
                            // Cap at 60 seconds to avoid unusually high values
                            remainingSeconds = Math.min(60, remainingSeconds);
                            sendTeleportWarning(player, remainingSeconds);
                        }
                    }

                    // Check if player should be teleported
                    if (afkStatus.getOrDefault(uuid, false) &&
                            timeSinceActive >= (afkThreshold + teleportThreshold) &&
                            !teleportedToAfk.getOrDefault(uuid, false)) {
                        teleportToAfkLocation(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }

    private void sendTeleportWarning(Player player, long seconds) {
        UUID uuid = player.getUniqueId();

        // Cancel any existing warning task
        BukkitTask existingTask = warningTasks.remove(uuid);
        if (existingTask != null) {
            existingTask.cancel();
        }

        // We only want to show countdown for the final 5 seconds
        // If more than 5 seconds remain, just send initial warning
        if (seconds > 5) {
            // Send an initial warning without countdown
            player.sendMessage(TextUtils.deserializeString(
                    "<gold>You will be teleported to the AFK area soon.</gold>"));

            // Schedule the actual countdown to start at 5 seconds
            warningTasks.put(uuid, new BukkitRunnable() {
                @Override
                public void run() {
                    // Start the 5-second countdown
                    if (player.isOnline() && afkStatus.getOrDefault(uuid, false) &&
                            !teleportedToAfk.getOrDefault(uuid, false)) {
                        // Only start the 5-second countdown once, right before teleport
                        sendTeleportWarning(player, 5);
                    }
                }
            }.runTaskLater(plugin, (seconds - 5) * 20)); // Convert seconds to ticks

            return;
        }

        // For the final 5 seconds, show a single countdown sequence
        // If the final countdown is already running, do nothing
        if (warningTasks.containsKey(uuid) && seconds == 5) {
            // Already have a countdown running
            return;
        }

        // Start the 5-second countdown sequence
        player.sendMessage(TextUtils.deserializeString(teleportWarningMessage,
                Placeholder.unparsed("time", String.valueOf(seconds))));

        if (seconds <= 1) {
            // No need to schedule more messages
            return;
        }

        // Schedule the entire countdown at once
        warningTasks.put(uuid, new BukkitRunnable() {
            private long countdown = seconds - 1; // Start at seconds-1

            @Override
            public void run() {
                if (countdown <= 0 || !player.isOnline() || !afkStatus.getOrDefault(uuid, false) ||
                        teleportedToAfk.getOrDefault(uuid, false)) {
                    cancel();
                    warningTasks.remove(uuid);
                    return;
                }

                player.sendMessage(TextUtils.deserializeString(teleportWarningMessage,
                        Placeholder.unparsed("time", String.valueOf(countdown))));

                countdown--;

                if (countdown <= 0) {
                    cancel();
                    warningTasks.remove(uuid);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L)); // Run every second (20 ticks)
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.hasChangedOrientation()) {
            updateActivity(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Schedule a sync task since this event is async
        Bukkit.getScheduler().runTask(plugin, () -> updateActivity(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        // Don't count /afk command as activity
        if (!event.getMessage().toLowerCase().startsWith("/afk")) {
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

        // Clean up tasks
        BukkitTask teleportTask = teleportTasks.remove(uuid);
        if (teleportTask != null) {
            teleportTask.cancel();
        }

        BukkitTask warningTask = warningTasks.remove(uuid);
        if (warningTask != null) {
            warningTask.cancel();
        }

        lastActivity.remove(uuid);
        afkStatus.remove(uuid);
        teleportedToAfk.remove(uuid);

        // Make sure data is saved
        plugin.getPlayerDataManager().markDataAsDirty(uuid);
    }

    public void updateActivity(Player player) {
        if (player == null || !player.isOnline()) return;

        UUID uuid = player.getUniqueId();
        lastActivity.put(uuid, System.currentTimeMillis());

        // If player was AFK, unmark them
        if (afkStatus.getOrDefault(uuid, false)) {
            setAfk(player, false);
        }

        // Cancel warning tasks if they exist
        BukkitTask warningTask = warningTasks.remove(uuid);
        if (warningTask != null) {
            warningTask.cancel();
        }
    }

    public void setAfk(Player player, boolean afk) {
        if (player == null || !player.isOnline()) return;

        UUID uuid = player.getUniqueId();

        // Don't do anything if status isn't changing
        if (afkStatus.getOrDefault(uuid, false) == afk) {
            return;
        }

        afkStatus.put(uuid, afk);

        if (afk) {
            // Player is now AFK
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
            playerData.setAfk(true);

            // Broadcast AFK message if enabled
            if (ConfigManager.getInstance().getAfkConfig().broadcastAfkMessages) {
                Bukkit.broadcast(TextUtils.deserializeString(afkMessage,
                        Placeholder.component("player", player.displayName())));
            } else {
                // Just inform the player
                player.sendMessage(TextUtils.deserializeString("<gray>You are now AFK.</gray>"));
            }

            // Schedule just the teleport task without a separate warning
            long teleportTime = teleportThreshold / 50; // Convert ms to ticks

            // Schedule actual teleport
            teleportTasks.put(uuid, new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline() && afkStatus.getOrDefault(uuid, false)) {
                        teleportToAfkLocation(player);
                    }
                }
            }.runTaskLater(plugin, teleportTime));

            // Warning will be handled by the main checker task
        } else {
            // Player is no longer AFK
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
            playerData.setAfk(false);

            // Broadcast return message if enabled
            if (ConfigManager.getInstance().getAfkConfig().broadcastAfkMessages) {
                Bukkit.broadcast(TextUtils.deserializeString(returnMessage,
                        Placeholder.component("player", player.displayName())));
            } else {
                // Just inform the player
                player.sendMessage(TextUtils.deserializeString("<gray>You are no longer AFK.</gray>"));
            }

            // Cancel teleport and warning tasks
            BukkitTask teleportTask = teleportTasks.remove(uuid);
            if (teleportTask != null) {
                teleportTask.cancel();
            }

            BukkitTask warningTask = warningTasks.remove(uuid);
            if (warningTask != null) {
                warningTask.cancel();
            }

            // Return from AFK location if applicable
            if (teleportedToAfk.getOrDefault(uuid, false)) {
                returnFromAfkLocation(player);
            }
        }

        // Mark player data as dirty to ensure persistence
        plugin.getPlayerDataManager().markDataAsDirty(uuid);
        plugin.getPlayerDataManager().savePlayerData(uuid);
    }

    private void teleportToAfkLocation(Player player) {
        if (player == null || !player.isOnline()) return;

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

        // Clear any pending teleport tasks
        BukkitTask task = teleportTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }

        double radius = ConfigManager.getInstance().getAfkConfig().locationRadius;

        Location randomizedLocation = BlockUtils.getRandomLocationNear(afkLocation, radius);

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
                    player.teleportAsync(randomizedLocation).thenAccept(success -> {
                        if (success) {
                            // Set the flag after teleportation is complete
                            teleportedToAfk.put(uuid, true);
                            player.sendMessage(TextUtils.deserializeString("<yellow>You have been teleported to the AFK area.</yellow>"));
                        }
                    });
                }
            }, 5L); // Wait 5 ticks (1/4 second) before teleporting
        } else {
            // Player is not sitting, teleport immediately
            player.teleportAsync(randomizedLocation).thenAccept(success -> {
                if (success) {
                    // Set the flag after teleportation is complete
                    teleportedToAfk.put(uuid, true);
                    player.sendMessage(TextUtils.deserializeString("<yellow>You have been teleported to the AFK area.</yellow>"));
                }
            });
        }
    }

    private void returnFromAfkLocation(Player player) {
        if (player == null || !player.isOnline()) return;

        UUID uuid = player.getUniqueId();

        // If not actually teleported, do nothing
        if (!teleportedToAfk.getOrDefault(uuid, false)) {
            return;
        }

        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
        Location previousLocation = playerData.getLastLocation();

        if (previousLocation != null && previousLocation.getWorld() != null) {
            player.sendMessage(TextUtils.deserializeString("<yellow>Returning you to your previous location...</yellow>"));

            player.teleportAsync(previousLocation).thenAccept(success -> {
                if (success) {
                    // Only reset the flag if teleport was successful
                    teleportedToAfk.put(uuid, false);
                    player.sendMessage(TextUtils.deserializeString("<green>You have been returned to your previous location.</green>"));

                    // Clear the saved location to avoid reusing it
                    playerData.setLastLocation(null);
                    plugin.getPlayerDataManager().markDataAsDirty(uuid);
                    plugin.getPlayerDataManager().savePlayerData(uuid);
                } else {
                    player.sendMessage(TextUtils.deserializeString("<red>Failed to return you to your previous location.</red>"));
                    plugin.getLogger().warning("Failed to teleport " + player.getName() + " back from AFK location");
                }
            });
        } else {
            teleportedToAfk.put(uuid, false);
            player.sendMessage(TextUtils.deserializeString("<red>Could not find your previous location.</red>"));
            plugin.getLogger().warning("No previous location found for " + player.getName() + " when returning from AFK");
        }
    }

    public boolean isAfk(UUID uuid) {
        return afkStatus.getOrDefault(uuid, false);
    }
}
