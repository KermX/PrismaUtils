package me.kermx.prismaUtils.integrations;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.Logger;

public class FlightService {

    private final List<BiFunction<Player, Location, Boolean>> flightCheckers = new ArrayList<>();
    private final Logger logger;
    private boolean hasFlightPlugins = false;
    private Plugin townyFlightPlugin;

    public FlightService(PluginManager pluginManager, Logger logger) {
        this.logger = logger;
        registerTownyFlight(pluginManager);
    }

    private void registerTownyFlight(PluginManager pm) {
        townyFlightPlugin = pm.getPlugin("TownyFlight");
        if (townyFlightPlugin != null && townyFlightPlugin.isEnabled()) {
            flightCheckers.add((player, location) -> {
                try {
                    com.gmail.llmdlio.townyflight.TownyFlightAPI api =
                            com.gmail.llmdlio.townyflight.TownyFlightAPI.getInstance();
                    return api.canFly(player, true);
                } catch (Exception e) {
                    logger.warning("Error checking TownyFlight permissions: " + e.getMessage());
                    return false; // Fail closed for flight permissions
                }
            });
            hasFlightPlugins = true;
            logger.info("TownyFlight integration enabled");
        }
    }

    /**
     * Check if a player can fly at a specific location
     * @param player The player to check
     * @param location The location to check
     * @return true if the player can fly, false otherwise
     */
    public boolean canPlayerFly(Player player, Location location) {

        // If no flight plugins, default to false
        if (!hasFlightPlugins) {
            return false;
        }

        // Check all registered flight plugins
        for (BiFunction<Player, Location, Boolean> checker : flightCheckers) {
            if (checker.apply(player, location)) {
                return true; // If any flight plugin allows, return true
            }
        }

        return false; // No flight plugins allowed flight
    }

    /**
     * Enable flight for a player using the appropriate plugin API
     * This preserves the enableFlight functionality from TownyFlightHookImpl
     * @param player The player to enable flight for
     */
    public void enablePlayerFlight(Player player) {
        if (townyFlightPlugin != null && townyFlightPlugin.isEnabled()) {
            try {
                com.gmail.llmdlio.townyflight.TownyFlightAPI api =
                        com.gmail.llmdlio.townyflight.TownyFlightAPI.getInstance();
                api.addFlight(player, true);
                player.setFlying(true);
            } catch (Exception e) {
                logger.warning("Error enabling flight with TownyFlight: " + e.getMessage());
            }
        } else {
            // Fallback if no flight plugins are available
            player.setAllowFlight(true);
            player.setFlying(true);
        }
    }

    /**
     * Disable flight for a player using the appropriate plugin API
     * This preserves the disableFlight functionality from TownyFlightHookImpl
     * @param player The player to disable flight for
     */
    public void disablePlayerFlight(Player player) {
        if (townyFlightPlugin != null && townyFlightPlugin.isEnabled()) {
            try {
                com.gmail.llmdlio.townyflight.TownyFlightAPI api =
                        com.gmail.llmdlio.townyflight.TownyFlightAPI.getInstance();
                api.removeFlight(player, true, false, "");
            } catch (Exception e) {
                logger.warning("Error disabling flight with TownyFlight: " + e.getMessage());
                // Fallback to vanilla method if API fails
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        } else {
            // Fallback if no flight plugins are available
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    /**
     * Check if any flight plugins are installed and enabled
     * @return true if any flight plugins are available
     */
    public boolean hasFlightPlugins() {
        return hasFlightPlugins;
    }
}
