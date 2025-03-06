package me.kermx.prismaUtils.integrations.flight.impl;

import com.gmail.llmdlio.townyflight.TownyFlightAPI;
import me.kermx.prismaUtils.integrations.flight.api.IFlightHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class TownyFlightHookImpl implements IFlightHook {
    private final TownyFlightAPI townyFlightAPI;

    private TownyFlightHookImpl(TownyFlightAPI api) {
        this.townyFlightAPI = api;
    }

    public static TownyFlightHookImpl createIfPresent(PluginManager pm) {
        Plugin townyFlightPlugin = pm.getPlugin("TownyFlight");
        if (townyFlightPlugin != null && townyFlightPlugin.isEnabled()) {
            try {
                TownyFlightAPI api = TownyFlightAPI.getInstance();
                return new TownyFlightHookImpl(api);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean canPlayerFly(Player player, Location location) {
        return townyFlightAPI.canFly(player,true);
    }

    public void enableFlight(Player player) {
        townyFlightAPI.addFlight(player, true);
        player.setFlying(true);
    }

    public void disableFlight(Player player) {
        townyFlightAPI.removeFlight(player,true,false,"");
    }
}