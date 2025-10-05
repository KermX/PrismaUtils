package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.managers.playerdata.PlayerData;
import me.kermx.prismaUtils.managers.playerdata.PlayerDataManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class LastLocationHandler implements Listener {
    private final PlayerDataManager dataManager;

    public LastLocationHandler(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location fromLocation = event.getFrom();

        if (event.getCause() != PlayerTeleportEvent.TeleportCause.UNKNOWN) {
            PlayerData playerData = dataManager.getPlayerData(player.getUniqueId());
            Location currentLastLocation = playerData.getLastLocation();

            boolean shouldUpdate = currentLastLocation == null ||
                    !fromLocation.getWorld().getName().equals(currentLastLocation.getWorld().getName()) ||
                    (Math.abs(fromLocation.getX() - currentLastLocation.getX()) > 1.0 ||
                            Math.abs(fromLocation.getZ() - currentLastLocation.getZ()) > 1.0);

            if (shouldUpdate) {
                Location storedLocation = fromLocation.clone();
                playerData.setLastLocation(storedLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Location deathLocation = player.getLocation();

        PlayerData playerData = dataManager.getPlayerData(player.getUniqueId());

        Location storedLocation = deathLocation.clone();
        playerData.setLastLocation(storedLocation);
    }
}
