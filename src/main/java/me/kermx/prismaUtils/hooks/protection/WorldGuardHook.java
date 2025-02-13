package me.kermx.prismaUtils.hooks.protection;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class WorldGuardHook {

    private final WorldGuardPlugin worldGuard;

    public WorldGuardHook(WorldGuardPlugin worldGuard) {
        this.worldGuard = worldGuard;
    }

    public static WorldGuardHook createIfPresent(PluginManager pm) {
        Plugin wg = pm.getPlugin("WorldGuard");
        if (wg != null && wg.isEnabled()) {
            return new WorldGuardHook((WorldGuardPlugin) wg);
        } else {
            return null;
        }
    }

    /**
     * Check if a player can build at a location
     * @param player the player
     * @param location the location
     * @return true if the player can build at the location
     */
    public boolean canBuildWorldGuard(Player player, Location location){
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return query.testState(BukkitAdapter.adapt(location), (LocalPlayer) BukkitAdapter.adapt(player), Flags.BLOCK_BREAK);
    }
}
