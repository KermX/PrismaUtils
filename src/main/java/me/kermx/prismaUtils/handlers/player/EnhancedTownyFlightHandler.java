package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.integrations.flight.FlightHandler;
import me.kermx.prismaUtils.integrations.flight.impl.TownyFlightHookImpl;
import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.managers.PlayerData.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EnhancedTownyFlightHandler implements Listener {
    private final FlightHandler flightHandler;
    private final PlayerDataManager dataManager;

    public EnhancedTownyFlightHandler(FlightHandler flightHandler, PlayerDataManager dataManager) {
        this.flightHandler = flightHandler;
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkAndUpdateFlight(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        checkAndUpdateFlight(event.getPlayer());
    }

    private void checkAndUpdateFlight(Player player) {
        PlayerData pData = dataManager.getPlayerData(player.getUniqueId());
        if (pData == null || !pData.isFlyEnabled()) {
            flightHandler.getHooks().stream()
                    .filter(hook -> hook instanceof TownyFlightHookImpl)
                    .findFirst()
                    .ifPresent(hook -> ((TownyFlightHookImpl) hook).disableFlight(player));
            return;
        }
        if (flightHandler.canPlayerFly(player, player.getLocation())) {
            flightHandler.getHooks().stream()
                    .filter(hook -> hook instanceof TownyFlightHookImpl)
                    .findFirst()
                    .ifPresent(hook -> ((TownyFlightHookImpl) hook).enableFlight(player));
        } else {
            flightHandler.getHooks().stream()
                    .filter(hook -> hook instanceof TownyFlightHookImpl)
                    .findFirst()
                    .ifPresent(hook -> ((TownyFlightHookImpl) hook).disableFlight(player));
        }
    }
}


