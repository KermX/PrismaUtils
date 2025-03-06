package me.kermx.prismaUtils.integrations.flight;

import me.kermx.prismaUtils.integrations.base.BaseIntegrationHandler;
import me.kermx.prismaUtils.integrations.flight.api.IFlightHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Logger;

public class FlightHandler extends BaseIntegrationHandler<IFlightHook> {

    public FlightHandler(PluginManager pm, Logger logger) {
        super(logger);
        // Maybe add world guard flag later, no need for now
        //loadIntegration(pm, "WorldGuard", "me.kermx.prismaUtils.integrations.flight.impl.WorldGuardFlightHookImpl");
        loadIntegration(pm, "TownyFlight", "me.kermx.prismaUtils.integrations.flight.impl.TownyFlightHookImpl");
    }

    /**
     * Checks if a player is allowed to fly at a specific location.
     *
     * @param player   The player attempting to fly.
     * @param location The location in question.
     * @return True if any hook allows flying at the location, false otherwise.
     */
    public boolean canPlayerFly(Player player, Location location) {
        for (IFlightHook hook : hooks) {
            if (hook.canPlayerFly(player, location)) {
                return true;
            }
        }
        return false;
    }
}
