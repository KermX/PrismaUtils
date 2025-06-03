package me.kermx.prismaUtils.handlers.player;

import com.gmail.llmdlio.townyflight.event.PlayerFlightChangeEvent;
import me.kermx.prismaUtils.integrations.FlightService;
import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.managers.PlayerData.PlayerDataManager;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import org.bukkit.Location;
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
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();

        // Check if the new world is in the whitelist
        if (!ConfigManager.getInstance().getMainConfig().flyWhitelistedWorlds.contains(worldName)) {
            // If not whitelisted, force disable flight regardless of other conditions
            flightService.disablePlayerFlight(player);
            PlayerData pData = dataManager.getPlayerData(player.getUniqueId());
            if (pData != null) {
                pData.setFlyEnabled(false);
            }
            return;
        }

        checkAndUpdateFlight(player);
    }


    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if(from.getWorld().equals(to.getWorld())) {
            double distanceSquared = from.distanceSquared(to);
            if(distanceSquared > 100) {
                return;
            }
        } else {
            // World changed, check if destination world is whitelisted
            String destinationWorld = to.getWorld().getName();
            if (!ConfigManager.getInstance().getMainConfig().flyWhitelistedWorlds.contains(destinationWorld)) {
                // If destination world is not whitelisted, ensure flight is disabled
                Player player = event.getPlayer();
                flightService.disablePlayerFlight(player);
                PlayerData pData = dataManager.getPlayerData(player.getUniqueId());
                if (pData != null) {
                    pData.setFlyEnabled(false);
                }
                return;
            }
        }

        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL ||
                cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            return;
        }

        checkAndUpdateFlight(event.getPlayer());
    }

    private void checkAndUpdateFlight(Player player) {
        PlayerData pData = dataManager.getPlayerData(player.getUniqueId());
        String worldName = player.getWorld().getName();

        // If world is not whitelisted, always disable flight
        if (!ConfigManager.getInstance().getMainConfig().flyWhitelistedWorlds.contains(worldName)) {
            flightService.disablePlayerFlight(player);
            if (pData != null) {
                pData.setFlyEnabled(false);
            }
            return;
        }

        // Continue with normal checks for whitelisted worlds
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