package me.kermx.prismaUtils.handlers.player;

import com.gmail.llmdlio.townyflight.event.PlayerFlightChangeEvent;
import me.kermx.prismaUtils.integrations.FlightService;
import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.managers.PlayerData.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EnhancedTownyFlightHandler implements Listener {
    private final FlightService flightService;
    private final PlayerDataManager dataManager;

    public EnhancedTownyFlightHandler(FlightService flightService, PlayerDataManager dataManager) {
        this.flightService = flightService;
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onPlayerFlightChangeEvent(PlayerFlightChangeEvent event) {
        Player player = event.getPlayer();
        PlayerData pData = dataManager.getPlayerData(player.getUniqueId());

        pData.setFlyEnabled(event.isFlightAllowed());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkAndUpdateFlight(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        checkAndUpdateFlight(event.getPlayer());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        checkAndUpdateFlight(event.getPlayer());
    }

    private void checkAndUpdateFlight(Player player) {
        PlayerData pData = dataManager.getPlayerData(player.getUniqueId());

        if (pData == null || !pData.isFlyEnabled()) {
            flightService.disablePlayerFlight(player);
            return;
        }

        if (flightService.canPlayerFly(player, player.getLocation())) {
            flightService.enablePlayerFlight(player);
        } else {
            flightService.disablePlayerFlight(player);
        }
    }
}
