package me.kermx.prismaUtils.integrations.protection;

import me.kermx.prismaUtils.integrations.base.BaseIntegrationHandler;
import me.kermx.prismaUtils.integrations.protection.api.IProtectionHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Logger;

public class ProtectionHandler extends BaseIntegrationHandler<IProtectionHook> {

    public ProtectionHandler(PluginManager pm, Logger logger) {
        super(logger);
        loadIntegration(pm, "WorldGuard", "me.kermx.prismaUtils.integrations.protection.impl.WorldGuardProtectionHookImpl");
        loadIntegration(pm, "Towny", "me.kermx.prismaUtils.integrations.protection.impl.TownyProtectionHookImpl");
    }

    /**
     * Checks if the block at a location is protected.
     *
     * @param player   The player attempting to interact with the location.
     * @param location The location in question.
     * @return True if any hook denies the interaction, false otherwise.
     */
    public boolean isLocationProtected(Player player, Location location) {
        for (IProtectionHook hook : hooks) {
            if (!hook.canBuild(player, location)) {
                return true;
            }
        }
        return false;
    }
}
