package me.kermx.prismaUtils.hooks;

import me.kermx.prismaUtils.hooks.protection.TownyHook;
import me.kermx.prismaUtils.hooks.protection.WorldGuardHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

public class ProtectionHandler {

    private final WorldGuardHook worldGuardHook;
    private final TownyHook townyHook;

    public ProtectionHandler(PluginManager pm) {
        this.worldGuardHook = WorldGuardHook.createIfPresent(pm);
        this.townyHook = TownyHook.createIfPresent(pm);
    }

    public boolean blockIsProtectedByPlugin(Player player, Location location) {
        if (worldGuardHook != null && !worldGuardHook.canBuildWorldGuard(player, location)) {
            return true;
        }
        if (townyHook != null && !townyHook.canBuildTowny(player, location)) {
            return true;
        }
        return false;
    }
}
