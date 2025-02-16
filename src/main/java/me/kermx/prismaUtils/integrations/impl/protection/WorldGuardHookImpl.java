package me.kermx.prismaUtils.integrations.impl.protection;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.kermx.prismaUtils.integrations.api.IProtectionHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class WorldGuardHookImpl implements IProtectionHook {

    private final WorldGuardPlugin worldGuard;

    private WorldGuardHookImpl(WorldGuardPlugin worldGuard) {
        this.worldGuard = worldGuard;
    }

    public static WorldGuardHookImpl createIfPresent(PluginManager pm) {
        Plugin wg = pm.getPlugin("WorldGuard");
        if (wg != null && wg.isEnabled()) {
            return new WorldGuardHookImpl((WorldGuardPlugin) wg);
        }
        return null;
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
        com.sk89q.worldedit.util.Location adaptedLocation = BukkitAdapter.adapt(location);
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(adaptedLocation);
        StateFlag.State state = set.queryState(localPlayer, Flags.BLOCK_BREAK);
        return state != StateFlag.State.DENY;
    }
}
