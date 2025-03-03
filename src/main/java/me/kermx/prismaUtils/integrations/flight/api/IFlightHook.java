package me.kermx.prismaUtils.integrations.flight.api;

import me.kermx.prismaUtils.integrations.base.api.IIntegrationHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IFlightHook extends IIntegrationHook {
    boolean canPlayerFly(Player player, Location location);
}

