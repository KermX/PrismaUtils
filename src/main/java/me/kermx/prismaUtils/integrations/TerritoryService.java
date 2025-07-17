package me.kermx.prismaUtils.integrations;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TerritoryService {

    private final Logger logger;
    private boolean hasTowny = false;
    private boolean hasWorldGuard = false;
    private Plugin townyPlugin;
    private Plugin worldGuardPlugin;

    public TerritoryService(PluginManager pluginManager, Logger logger) {
        this.logger = logger;
        registerTowny(pluginManager);
        registerWorldGuard(pluginManager);
    }

    private void registerTowny(PluginManager pm) {
        townyPlugin = pm.getPlugin("Towny");
        if (townyPlugin != null && townyPlugin.isEnabled()) {
            hasTowny = true;
            logger.info("Towny integration enabled");
        }
    }

    private void registerWorldGuard(PluginManager pm) {
        worldGuardPlugin = pm.getPlugin("WorldGuard");
        if (worldGuardPlugin != null && worldGuardPlugin.isEnabled()) {
            hasWorldGuard = true;
            logger.info("WorldGuard integration enabled");
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
     * Checks if flight is allowed at the given location based on WorldGuard regions
     * @param player The player to check
     * @param location The location to check
     * @return true if flight is allowed, false if denied by a region
     */
    public boolean isFlightAllowed(Player player, Location location) {
        if (!hasWorldGuard) {
            return true; // No WorldGuard, allow flight
        }

        try {
            com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();
            com.sk89q.worldguard.bukkit.WorldGuardPlugin wgp = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) worldGuardPlugin;
            com.sk89q.worldguard.LocalPlayer localPlayer = wgp.wrapPlayer(player);
            com.sk89q.worldguard.protection.regions.RegionContainer container = wg.getPlatform().getRegionContainer();
            com.sk89q.worldguard.protection.regions.RegionQuery query = container.createQuery();
            com.sk89q.worldedit.util.Location loc = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location);
            com.sk89q.worldedit.world.World world = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location.getWorld());

            // Check if player has bypass
            if (wg.getPlatform().getSessionManager().hasBypass(localPlayer, world)) {
                return true;
            }

            // Try to check for flight-related flags dynamically
            Boolean flightFlagResult = checkFlightFlags(query, loc, localPlayer);
            if (flightFlagResult != null) {
                return flightFlagResult;
            }

            // Check region names for flight restrictions
            java.util.Set<com.sk89q.worldguard.protection.regions.ProtectedRegion> regions = query.getApplicableRegions(loc).getRegions();

            for (com.sk89q.worldguard.protection.regions.ProtectedRegion region : regions) {
                String regionId = region.getId().toLowerCase();
                if (regionId.contains("no-fly") ||
                        regionId.contains("noflight") ||
                        regionId.contains("no_fly") ||
                        regionId.contains("nofly")) {
                    return false;
                }
            }

            return true; // No restrictions found

        } catch (Exception e) {
            logger.warning("Error checking flight permissions: " + e.getMessage());
            return true; // Fail open on error
        }
    }

    /**
     * Dynamically checks for flight-related flags in WorldGuard
     * @param query The region query
     * @param loc The location
     * @param localPlayer The local player
     * @return Boolean result if a flag was found and checked, null if no flags exist
     */
    private Boolean checkFlightFlags(com.sk89q.worldguard.protection.regions.RegionQuery query,
                                     com.sk89q.worldedit.util.Location loc,
                                     com.sk89q.worldguard.LocalPlayer localPlayer) {

        // List of possible flight flag names to check for
        String[] possibleFlagNames = {"fly", "flight", "allow-flight", "allow_flight", "can-fly", "can_fly"};

        try {
            // Get the WorldGuard instance and flag registry
            com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();
            com.sk89q.worldguard.protection.flags.registry.FlagRegistry flagRegistry = wg.getFlagRegistry();

            for (String flagName : possibleFlagNames) {
                try {
                    // Try to get the flag by name from the flag registry
                    com.sk89q.worldguard.protection.flags.Flag<?> flag = flagRegistry.get(flagName);

                    if (flag != null) {
                        Object flagValue = query.queryValue(loc, localPlayer, flag);
                        if (flagValue != null) {
                            if (flagValue instanceof Boolean) {
                                logger.fine("Found flight flag '" + flagName + "' with value: " + flagValue);
                                return (Boolean) flagValue;
                            } else if (flagValue instanceof com.sk89q.worldguard.protection.flags.StateFlag.State) {
                                com.sk89q.worldguard.protection.flags.StateFlag.State state =
                                        (com.sk89q.worldguard.protection.flags.StateFlag.State) flagValue;
                                logger.fine("Found flight flag '" + flagName + "' with state: " + state);
                                return state == com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Flag doesn't exist or error accessing it, continue to next flag
                    logger.fine("Flight flag '" + flagName + "' not found or error accessing: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.fine("Error accessing flag registry: " + e.getMessage());
        }

        // Try to access flags through the static Flags class if it exists
        try {
            java.lang.reflect.Field flagsField = com.sk89q.worldguard.protection.flags.Flags.class.getDeclaredField("FLY");
            if (flagsField != null) {
                com.sk89q.worldguard.protection.flags.Flag<?> flyFlag =
                        (com.sk89q.worldguard.protection.flags.Flag<?>) flagsField.get(null);
                if (flyFlag != null) {
                    Object flagValue = query.queryValue(loc, localPlayer, flyFlag);
                    if (flagValue != null) {
                        if (flagValue instanceof Boolean) {
                            logger.fine("Found FLY flag via reflection with value: " + flagValue);
                            return (Boolean) flagValue;
                        } else if (flagValue instanceof com.sk89q.worldguard.protection.flags.StateFlag.State) {
                            com.sk89q.worldguard.protection.flags.StateFlag.State state =
                                    (com.sk89q.worldguard.protection.flags.StateFlag.State) flagValue;
                            logger.fine("Found FLY flag via reflection with state: " + state);
                            return state == com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // FLY flag doesn't exist in this WorldGuard version, which is expected
            logger.fine("FLY flag not found via reflection (expected for base WorldGuard): " + e.getMessage());
        }

        return null; // No flight flags found
    }

    /**
     * Checks if the location is in a WorldGuard region that denies flight
     * @param player The player to check
     * @param location The location to check
     * @return true if in a no-flight region, false otherwise
     */
    public boolean isInNoFlightRegion(Player player, Location location) {
        return !isFlightAllowed(player, location);
    }

    /**
     * Gets the names of all WorldGuard regions at the given location
     * @param location The location to check
     * @return A list of region names, or empty list if no regions or WorldGuard not available
     */
    public List<String> getRegionNames(Location location) {
        List<String> regionNames = new ArrayList<>();

        if (!hasWorldGuard) {
            return regionNames;
        }

        try {
            com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();
            com.sk89q.worldguard.protection.regions.RegionContainer container = wg.getPlatform().getRegionContainer();
            com.sk89q.worldguard.protection.regions.RegionQuery query = container.createQuery();
            com.sk89q.worldedit.util.Location loc = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location);

            java.util.Set<com.sk89q.worldguard.protection.regions.ProtectedRegion> regions = query.getApplicableRegions(loc).getRegions();

            for (com.sk89q.worldguard.protection.regions.ProtectedRegion region : regions) {
                regionNames.add(region.getId());
            }

        } catch (Exception e) {
            logger.warning("Error getting region names: " + e.getMessage());
        }

        return regionNames;
    }

    /**
     * Checks if the location is in a specific WorldGuard region
     * @param location The location to check
     * @param regionName The name of the region to check for
     * @return true if the location is in the specified region, false otherwise
     */
    public boolean isInRegion(Location location, String regionName) {
        List<String> regions = getRegionNames(location);
        return regions.stream().anyMatch(name -> name.equalsIgnoreCase(regionName));
    }

    /**
     * Checks if Towny is available
     * @return true if Towny is loaded and enabled
     */
    public boolean isTownyAvailable() {
        return hasTowny;
    }

    /**
     * Checks if WorldGuard is available
     * @return true if WorldGuard is loaded and enabled
     */
    public boolean isWorldGuardAvailable() {
        return hasWorldGuard;
    }
}
