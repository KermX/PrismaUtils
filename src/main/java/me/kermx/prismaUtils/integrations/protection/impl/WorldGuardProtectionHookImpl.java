package me.kermx.prismaUtils.integrations.protection.impl;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.kermx.prismaUtils.integrations.protection.api.IProtectionHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class WorldGuardProtectionHookImpl implements IProtectionHook {

    private final WorldGuardPlugin worldGuard;

    private WorldGuardProtectionHookImpl(WorldGuardPlugin worldGuard) {
        this.worldGuard = worldGuard;
    }

    public static WorldGuardProtectionHookImpl createIfPresent(PluginManager pm) {
        Plugin wg = pm.getPlugin("WorldGuard");
        if (wg != null && wg.isEnabled()) {
            return new WorldGuardProtectionHookImpl((WorldGuardPlugin) wg);
        }
        return null;
    }

    /**
     * @param player
     * @param location
     * @return false if the player cannot build, true if they can
     */
    @Override
    public boolean canBuild(Player player, Location location) {
        LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
        com.sk89q.worldedit.util.Location adaptedLocation = BukkitAdapter.adapt(location);
        World adaptedWorld = BukkitAdapter.adapt(location.getWorld());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        boolean canBypass = WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer,adaptedWorld);

        if (canBypass) {return true;}
        return query.testState(adaptedLocation, localPlayer, Flags.BUILD);
    }
}
