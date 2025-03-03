package me.kermx.prismaUtils.integrations.flight.impl;

import com.palmergames.bukkit.towny.TownyAPI;
import me.kermx.prismaUtils.integrations.flight.api.IFlightHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class TownyFlightHookImpl implements IFlightHook {
    private final TownyAPI townyAPI;

    public TownyFlightHookImpl() {
        this.townyAPI = TownyAPI.getInstance();
    }

    public static TownyFlightHookImpl createIfPresent(PluginManager pm) {
        Plugin townyPlugin = pm.getPlugin("Towny");
        if (townyPlugin != null && townyPlugin.isEnabled()) {
            return new TownyFlightHookImpl();
        }
        return null;
    }

    @Override
    public boolean canPlayerFly(Player player, Location location) {
        return townyAPI.isWilderness(location);
    }
}
