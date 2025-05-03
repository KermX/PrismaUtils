package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.managers.PlayerData.PlayerDataManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerDataListener implements Listener {
    private final PlayerDataManager dataManager;

    public PlayerDataListener(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Data will be loaded lazily when needed and listener is registered internally
        PlayerData playerData = dataManager.getPlayerData(playerId);
        if (!playerData.getMailbox().isEmpty()) {
            player.sendMessage(TextUtils.deserializeString("<green>You have " +
                    playerData.getMailbox().size() + " unread messages. Use /mail read to view them."));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        PlayerData data = dataManager.getPlayerData(playerId);
        if (data != null) {
            // Update any final state - this will automatically trigger listeners
            data.setFlyEnabled(player.getAllowFlight());

            // The actual savePlayerData and removePlayerData calls will
            // handle unregistering listeners and persisting data
            dataManager.savePlayerData(playerId);
            dataManager.removePlayerData(playerId);
        }
    }
}