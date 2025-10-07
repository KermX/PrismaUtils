package me.kermx.prismaUtils.services;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Logger;

public class TerritoryService {

    private final Logger logger;
    private boolean hasTowny = false;
    private Plugin townyPlugin;

    public TerritoryService(PluginManager pluginManager, Logger logger) {
        this.logger = logger;
        registerTowny(pluginManager);
    }

    private void registerTowny(PluginManager pm) {
        townyPlugin = pm.getPlugin("Towny");
        if (townyPlugin != null && townyPlugin.isEnabled()) {
            hasTowny = true;
            logger.info("Towny integration enabled");
        }
    }

    /**
     * Gets the type of Towny plot at the given location
     * @param location The location to check
     * @return The plot type as a string, or null if not in Towny or an error occurs
     */
    public String getTownyPlotType(Location location) {
        if (!hasTowny) {
            return null;
        }

        try {
            com.palmergames.bukkit.towny.TownyAPI townyAPI = com.palmergames.bukkit.towny.TownyAPI.getInstance();

            // Check if in wilderness
            if (townyAPI.isWilderness(location)) {
                return "WILDS";
            }

            // Get the town block
            com.palmergames.bukkit.towny.object.TownBlock townBlock = townyAPI.getTownBlock(location);
            if (townBlock == null) {
                return "WILDS";
            }

            // Get plot type
            com.palmergames.bukkit.towny.object.TownBlockType plotType = townBlock.getType();
            return plotType.toString();

        } catch (Exception e) {
            logger.warning("Error getting Towny plot type: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if the player is in a specific type of Towny plot
     * @param location The location to check
     * @param plotType The plot type to check for (e.g., "RESIDENTIAL", "COMMERCIAL", "INDUSTRIAL", "WILDERNESS")
     * @return true if the location is the specified plot type, false otherwise
     */
    public boolean isInTownyPlotType(Location location, String plotType) {
        String currentPlotType = getTownyPlotType(location);
        return currentPlotType != null && currentPlotType.equalsIgnoreCase(plotType);
    }

    /**
     * Checks if the player is in Towny wilderness
     * @param location The location to check
     * @return true if in wilderness, false otherwise
     */
    public boolean isInWilderness(Location location) {
        return isInTownyPlotType(location, "WILDS");
    }

    /**
     * Gets the Towny town name at the given location
     * @param location The location to check
     * @return The town name, or null if not in a town or an error occurs
     */
    public String getTownName(Location location) {
        if (!hasTowny) {
            return null;
        }

        try {
            com.palmergames.bukkit.towny.TownyAPI townyAPI = com.palmergames.bukkit.towny.TownyAPI.getInstance();

            if (townyAPI.isWilderness(location)) {
                return null;
            }

            com.palmergames.bukkit.towny.object.Town town = townyAPI.getTown(location);
            return town != null ? town.getName() : null;

        } catch (Exception e) {
            logger.warning("Error getting town name: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if Towny is available
     * @return true if Towny is loaded and enabled
     */
    public boolean isTownyAvailable() {
        return hasTowny;
    }
}
