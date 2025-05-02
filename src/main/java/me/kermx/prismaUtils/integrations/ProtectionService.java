package me.kermx.prismaUtils.integrations;

import com.palmergames.bukkit.towny.object.TownyPermission;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ProtectionService {

    private final List<BiFunction<Player, Location, Boolean>> protectionCheckers = new ArrayList<>();
    private boolean hasProtectionPlugins = false;

    public ProtectionService(PluginManager pluginManager) {
        registerTowny(pluginManager);
        registerWorldGuard(pluginManager);
    }

    private void registerTowny(PluginManager pm) {
        Plugin townyPlugin = pm.getPlugin("Towny");
        if (townyPlugin != null && townyPlugin.isEnabled()) {
            protectionCheckers.add((player, location) -> {
                try {
                    // Direct call to Towny API
                    return com.palmergames.bukkit.towny.utils.PlayerCacheUtil.getCachePermission(
                            player,
                            location,
                            location.getBlock().getType(),
                            com.palmergames.bukkit.towny.object.TownyPermission.ActionType.BUILD
                    );
                } catch (Exception e) {
                    return true; // Fail open on error
                }
            });
            hasProtectionPlugins = true;
        }
    }

    private void registerWorldGuard(PluginManager pm) {
        Plugin wgPlugin = pm.getPlugin("WorldGuard");
        if (wgPlugin != null && wgPlugin.isEnabled()) {
            protectionCheckers.add((player, location) -> {
                try {
                    // Direct call to WorldGuard API
                    com.sk89q.worldguard.WorldGuard wg = com.sk89q.worldguard.WorldGuard.getInstance();
                    com.sk89q.worldguard.bukkit.WorldGuardPlugin wgp = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) wgPlugin;
                    com.sk89q.worldguard.LocalPlayer localPlayer = wgp.wrapPlayer(player);
                    com.sk89q.worldguard.protection.regions.RegionContainer container = wg.getPlatform().getRegionContainer();
                    com.sk89q.worldguard.protection.regions.RegionQuery query = container.createQuery();
                    com.sk89q.worldedit.util.Location loc = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location);
                    com.sk89q.worldedit.world.World world = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location.getWorld());

                    if (wg.getPlatform().getSessionManager().hasBypass(localPlayer, world)) {
                        return true;
                    }
                    return query.testState(loc, localPlayer, com.sk89q.worldguard.protection.flags.Flags.BUILD);
                } catch (Exception e) {
                    return true; // Fail open on error
                }
            });
            hasProtectionPlugins = true;
        }
    }

    /**
     * Checks if a player can build at a location
     * @param player The player to check
     * @param location The location to check
     * @return true if the player can build, false if they cannot
     */
    public boolean canBuild(Player player, Location location) {
        // Admin override check
        if (player.hasPermission("prismautils.bypass.protection")) {
            return true;
        }

        // If no protection plugins, default to true
        if (!hasProtectionPlugins) {
            return true;
        }

        // Check all registered protection plugins
        for (BiFunction<Player, Location, Boolean> checker : protectionCheckers) {
            if (!checker.apply(player, location)) {
                return false; // If any protection plugin denies, return false
            }
        }

        return true; // All checks passed
    }

    public boolean isLocationProtected(Player player, Location location) {
        return !canBuild(player, location);
    }


}
