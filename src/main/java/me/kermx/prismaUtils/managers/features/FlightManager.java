package me.kermx.prismaUtils.managers.features;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.integrations.TerritoryService;
import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FlightManager implements Listener {

    private final PrismaUtils plugin;
    private final TerritoryService territoryService;
    private final Map<UUID, Boolean> lastFlightState = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastPlotType = new ConcurrentHashMap<>();

    // Fall damage protection system
    private final Map<UUID, Long> fallProtectionPlayers = new ConcurrentHashMap<>();
    private static final long FALL_PROTECTION_DURATION = 10000; // 10 seconds in milliseconds

    public FlightManager(PrismaUtils plugin, TerritoryService territoryService) {
        this.plugin = plugin;
        this.territoryService = territoryService;

        startTempFlightCountdown();
        startFallProtectionCleanup();
        startFlightPermissionChecker();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Start a periodic task to check flight permissions for all players
     */
    private void startFlightPermissionChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();

                    // Skip dead players
                    if (player.isDead()) {
                        continue;
                    }

                    // Check if flight state should change
                    boolean currentCanFly = canPlayerFly(player);
                    Boolean lastCanFly = lastFlightState.get(uuid);

                    if (lastCanFly == null || lastCanFly != currentCanFly) {
                        lastFlightState.put(uuid, currentCanFly);
                        updatePlayerFlightState(player, lastCanFly != null ? lastCanFly : false);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 60L); // Check every 3 seconds (60 ticks)
    }

    /**
     * Add fall damage protection to a player
     * @param player The player to protect
     */
    private void addFallProtection(Player player) {
        UUID uuid = player.getUniqueId();
        long expireTime = System.currentTimeMillis() + FALL_PROTECTION_DURATION;
        fallProtectionPlayers.put(uuid, expireTime);
    }

    /**
     * Check if a player has fall damage protection
     * @param player The player to check
     * @return true if player has active fall protection
     */
    private boolean hasFallProtection(Player player) {
        UUID uuid = player.getUniqueId();
        Long expireTime = fallProtectionPlayers.get(uuid);

        if (expireTime == null) {
            return false;
        }

        if (System.currentTimeMillis() > expireTime) {
            fallProtectionPlayers.remove(uuid);
            return false;
        }

        return true;
    }

    /**
     * Remove fall damage protection from a player
     * @param player The player to remove protection from
     */
    private void removeFallProtection(Player player) {
        fallProtectionPlayers.remove(player.getUniqueId());
    }

    /**
     * Start a task to clean up expired fall protection entries
     */
    private void startFallProtectionCleanup() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                fallProtectionPlayers.entrySet().removeIf(entry -> currentTime > entry.getValue());
            }
        }.runTaskTimer(plugin, 200L, 200L); // Run every 10 seconds
    }

    /**
     * Check if a player can fly at their current location
     * This now considers both temp flight and location permissions
     */
    public boolean canPlayerFly(Player player) {
        if (player.hasPermission("prismautils.fly.admin")) {
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            // Admin can fly if they have flight enabled or temp flight
            return playerData.isFlightEnabled();
        }

        String worldName = player.getWorld().getName();
        if (!ConfigManager.getInstance().getMainConfig().flyWhitelistedWorlds.contains(worldName)) {
            return false; // No flight allowed in non-whitelisted worlds, including temp flight
        }

        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Check if player has temporary flight (bypasses location restrictions)
        if (playerData.hasTempFlight()) {
            return true;
        }

        // Check if player has permanent flight enabled AND has location permission
        if (playerData.isFlightEnabled()) {
            return hasLocationPermission(player);
        }

        return false;
    }

    /**
     * Check if player has permission to fly at their current location
     */
    private boolean hasLocationPermission(Player player) {
        // Admin bypass permission
        if (player.hasPermission("prismautils.fly.admin")) {
            return true;
        }

        String worldName = player.getWorld().getName();
        if (!ConfigManager.getInstance().getMainConfig().flyWhitelistedWorlds.contains(worldName)) {
            return false; // Flight not allowed in non-whitelisted worlds
        }


        // Global flight permission (works everywhere)
        if (player.hasPermission("prismautils.fly.global")) {
            return true;
        }

        // Check Towny-based permissions
        if (territoryService.isTownyAvailable()) {
            String plotType = territoryService.getTownyPlotType(player.getLocation());

            if (player.hasPermission("prismautils.fly.town")) {
                return plotType != null && !plotType.equals("WILDS");
            }
            if (player.hasPermission("prismautils.fly.wilderness")) {
                return "WILDS".equals(plotType);
            }
            // Specific plot type permissions
            if (plotType != null) {
                String plotPermission = "prismautils.fly.plot." + plotType.toLowerCase();
                if (player.hasPermission(plotPermission)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Toggle flight for a player
     */
    public void toggleFlight(Player player) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Toggle based on the stored preference
        boolean currentPreference = playerData.isFlightEnabled();
        setFlightEnabled(player, !currentPreference);
    }

    /**
     * Set permanent flight state for a player
     */
    public void setFlightEnabled(Player player, boolean enabled) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (enabled) {
            if (!player.hasPermission("prismautils.fly.admin")) {
                // Check if world is whitelisted for non-admins
                String worldName = player.getWorld().getName();
                if (!ConfigManager.getInstance().getMainConfig().flyWhitelistedWorlds.contains(worldName)) {
                    player.sendMessage(TextUtils.deserializeString(
                            "<red>Flight is not allowed in this world.</red>"
                    ));
                    return;
                }
            }


            // Player wants to enable flight
            playerData.setFlightEnabled(true);

            // Check if they can actually fly at their current location
            if (canPlayerFly(player)) {
                player.setAllowFlight(true);
                player.sendMessage(TextUtils.deserializeString("<green>Flight enabled!</green>"));
            } else {
                // They want flight but can't use it here, so disable it in data
                playerData.setFlightEnabled(false);
                player.setAllowFlight(false);
                player.setFlying(false);
                player.sendMessage(TextUtils.deserializeString("<yellow>You don't have permission to fly at your current location.</yellow>"));
            }
        } else {
            // Player wants to disable flight
            playerData.setFlightEnabled(false);

            // If player is currently flying, give them fall protection
            if (player.isFlying()) {
                addFallProtection(player);
            }

            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage(TextUtils.deserializeString("<red>Flight disabled!</red>"));
        }
    }


    /**
     * Add temporary flight time to a player
     */
    public void addTempFlightTime(Player player, long seconds) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        playerData.addTempFlightSeconds(seconds);

        // Update flight state immediately
        updatePlayerFlightState(player);

        // Notify player
        long totalSeconds = playerData.getTempFlightSeconds();
        String timeStr = formatTime(totalSeconds);

        player.sendMessage(TextUtils.deserializeString(
                "<green>Added <white><time></white> of temporary flight time! Total: <white><total></white></green>",
                Placeholder.unparsed("time", formatTime(seconds)),
                Placeholder.unparsed("total", timeStr)
        ));
    }

    /**
     * Update a player's flight state based on their current permissions and location
     */
    private void updatePlayerFlightState(Player player, boolean previouslyAllowed, boolean showMessage) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        boolean canFly = canPlayerFly(player);

        if (canFly) {
            // Player can fly at this location
            player.setAllowFlight(true);
        } else {
            // Player cannot fly at this location
            boolean wasFlying = player.isFlying();

            // If they had flight enabled, disable it and notify
            if (playerData.isFlightEnabled()) {
                playerData.setFlightEnabled(false);

                // Show message if they lost flight due to location change
                if (previouslyAllowed && showMessage) {
                    player.sendMessage(TextUtils.deserializeString(
                            "<yellow>Flight disabled due to leaving allowed area.</yellow>"
                    ));
                }
            }

            // If player was flying when flight was disabled, give fall protection
            if (wasFlying) {
                addFallProtection(player);
            }

            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    /**
     * Update a player's flight state based on their current permissions and location (overloaded method)
     */
    private void updatePlayerFlightState(Player player) {
        updatePlayerFlightState(player, player.getAllowFlight(), false);
    }

    /**
     * Update a player's flight state based on their current permissions and location (overloaded method)
     */
    private void updatePlayerFlightState(Player player, boolean previouslyAllowed) {
        updatePlayerFlightState(player, previouslyAllowed, true);
    }

    /**
     * Check if player is in a location where temp flight should count down
     */
    private boolean shouldCountTempFlight(Player player) {
        // First check if the world is whitelisted
        String worldName = player.getWorld().getName();
        if (!ConfigManager.getInstance().getMainConfig().flyWhitelistedWorlds.contains(worldName)) {
            return false; // Don't count down temp flight in non-whitelisted worlds
        }

        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Only count down temp flight if they have it and are in a whitelisted world
        return playerData.hasTempFlight();
    }


    /**
     * Start the temporary flight countdown task
     */
    private void startTempFlightCountdown() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

                    if (playerData.hasTempFlight() && shouldCountTempFlight(player)) {
                        // Decrement temp flight time
                        long newSeconds = playerData.getTempFlightSeconds() - 1;
                        playerData.setTempFlightSeconds(newSeconds);

                        if (newSeconds <= 0) {
                            // Temp flight expired
                            boolean wasFlying = player.isFlying();

                            player.sendMessage(TextUtils.deserializeString("<red>Temporary flight time expired!</red>"));

                            // If player was flying when temp flight expired, give fall protection
                            if (wasFlying) {
                                addFallProtection(player);
                            }

                            // Update flight state since temp flight is gone
                            updatePlayerFlightState(player);
                        } else if (newSeconds <= 10) {
                            // Warning for last 10 seconds
                            player.sendMessage(TextUtils.deserializeString(
                                    "<yellow>Temporary flight expires in <time> seconds!</yellow>",
                                    Placeholder.unparsed("time", String.valueOf(newSeconds))
                            ));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
    }

    /**
     * Format time in seconds to a readable string
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + "m " + remainingSeconds + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long remainingSeconds = seconds % 60;
            return hours + "h " + minutes + "m " + remainingSeconds + "s";
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // Only handle player fall damage
        if (!(event.getEntity() instanceof Player) || event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Check if player has fall protection
        if (hasFallProtection(player)) {
            event.setCancelled(true);
            removeFallProtection(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Update flight state on join
        updatePlayerFlightState(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastFlightState.remove(uuid);
        lastPlotType.remove(uuid);
        fallProtectionPlayers.remove(uuid);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Schedule a task to run after the respawn completes
        // This ensures the player's location is properly updated
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);

                // Check if player had flight enabled (permanent or temporary)
                if (playerData.isFlightEnabled() || playerData.hasTempFlight()) {
                    // Check if they can fly at the respawn location
                    if (canPlayerFly(player)) {
                        player.setAllowFlight(true);
                        // Don't set flying mode - let them activate it manually
                    } else {
                        // If they can't fly at respawn location, disable permanent flight
                        if (playerData.isFlightEnabled()) {
                            playerData.setFlightEnabled(false);
                            player.sendMessage(TextUtils.deserializeString(
                                    "<yellow>Flight disabled - you don't have permission to fly at this location.</yellow>"
                            ));
                        }
                        // Temp flight remains in data but can't be used
                        player.setAllowFlight(false);
                    }
                }

                // Update flight state tracking
                boolean currentCanFly = canPlayerFly(player);
                lastFlightState.put(uuid, currentCanFly);
            }
        }, 1L); // Wait 1 tick to ensure respawn is complete
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Only process if the teleport was successful and the player actually moved to a different location
        if (event.isCancelled()) {
            return;
        }

        // Get the current flight state before teleport
        boolean previousCanFly = lastFlightState.getOrDefault(uuid, false);

        // Schedule a task to run after the teleport completes
        // This ensures the player's location is properly updated
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
                String worldName = player.getWorld().getName();

                // Check for admin bypass first
                if (player.hasPermission("prismautils.fly.admin")) {
                    // Admin can fly anywhere
                    boolean currentCanFly = canPlayerFly(player);
                    lastFlightState.put(uuid, currentCanFly);

                    // If admin had flight enabled, restore ability to fly but not flying mode
                    if (playerData.isFlightEnabled() || playerData.hasTempFlight()) {
                        player.setAllowFlight(true);
                        // Don't set flying mode - let them activate it manually
                    }
                    return;
                }

                // Check if the new world is whitelisted
                if (!ConfigManager.getInstance().getMainConfig().flyWhitelistedWorlds.contains(worldName)) {
                    // If not whitelisted, force disable flight
                    boolean wasFlying = player.isFlying();

                    if (playerData.isFlightEnabled()) {
                        playerData.setFlightEnabled(false);
                    }

                    // If player was flying when teleporting to restricted world, give fall protection
                    if (wasFlying) {
                        addFallProtection(player);
                    }

                    player.setAllowFlight(false);
                    player.setFlying(false);
                    lastFlightState.put(uuid, false);

                    // Show appropriate message
                    if (playerData.hasTempFlight()) {
                        player.sendMessage(TextUtils.deserializeString(
                                "<yellow>Temporary flight disabled - this world does not allow flight.</yellow>"
                        ));
                    } else if (previousCanFly) {
                        player.sendMessage(TextUtils.deserializeString(
                                "<yellow>Flight disabled - this world does not allow flight.</yellow>"
                        ));
                    }
                    return;
                }

                // World is whitelisted, check if flight should be enabled
                boolean currentCanFly = canPlayerFly(player);
                lastFlightState.put(uuid, currentCanFly);

                // Only restore flight if they had permanent flight enabled
                // Temp flight should require manual re-activation
                if (currentCanFly && playerData.isFlightEnabled()) {
                    player.setAllowFlight(true);
                    // Don't set flying mode - let them activate it manually
                } else if (previousCanFly != currentCanFly) {
                    // Update flight state if it changed
                    updatePlayerFlightState(player, previousCanFly, true);
                }
            }
        }, 1L); // Wait 1 tick to ensure teleport is complete
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String worldName = player.getWorld().getName();

        // Get the current flight state before world change
        boolean previousCanFly = lastFlightState.getOrDefault(uuid, false);

        // Check for admin bypass first
        if (player.hasPermission("prismautils.fly.admin")) {
            // Admin can fly anywhere, just update normal flight permissions
            boolean currentCanFly = canPlayerFly(player);
            lastFlightState.put(uuid, currentCanFly);

            // For admins, if they had flight enabled, restore ability to fly but not flying mode
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
            if (playerData.isFlightEnabled() || playerData.hasTempFlight()) {
                player.setAllowFlight(true);
                // Don't set flying mode - let them activate it manually
            }
            return;
        }

        // Check if the new world is whitelisted for non-admins
        if (!ConfigManager.getInstance().getMainConfig().flyWhitelistedWorlds.contains(worldName)) {
            // If not whitelisted, force disable flight regardless of other conditions
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
            boolean wasFlying = player.isFlying();

            if (playerData.isFlightEnabled()) {
                playerData.setFlightEnabled(false);
            }

            // If player was flying when changing to restricted world, give fall protection
            if (wasFlying) {
                addFallProtection(player);
            }

            player.setAllowFlight(false);
            player.setFlying(false);
            lastFlightState.put(uuid, false);

            // Show appropriate message based on what they had
//            if (playerData.hasTempFlight()) {
//                player.sendMessage(TextUtils.deserializeString(
//                        "<yellow>Temporary flight disabled - this world does not allow flight.</yellow>"
//                ));
//            } else if (previousCanFly) {
//                player.sendMessage(TextUtils.deserializeString(
//                        "<yellow>Flight disabled - this world does not allow flight.</yellow>"
//                ));
//            }
            return;
        }

        // World is whitelisted, check if player should have flight
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
        boolean currentCanFly = canPlayerFly(player);
        lastFlightState.put(uuid, currentCanFly);

        // Only restore flight if they had permanent flight enabled
        // Temp flight should require manual re-activation
        if (currentCanFly && playerData.isFlightEnabled()) {
            player.setAllowFlight(true);
            // Don't set flying mode - let them activate it manually
        } else if (previousCanFly != currentCanFly) {
            // Update flight state if it changed
            updatePlayerFlightState(player, previousCanFly, true);
        }
    }


    /**
     * Get remaining temp flight time for a player
     */
    public long getRemainingTempFlightTime(Player player) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        return playerData.getTempFlightSeconds();
    }

    /**
     * Check if player has permanent flight enabled
     */
    public boolean hasFlightEnabled(Player player) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        return playerData.isFlightEnabled();
    }
}
