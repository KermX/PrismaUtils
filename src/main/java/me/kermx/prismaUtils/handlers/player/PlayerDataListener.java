package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.managers.PlayerData.PlayerDataManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerDataListener implements Listener {
    private final PlayerDataManager dataManager;

    public PlayerDataListener(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        dataManager.loadPlayerData(playerId);

        PlayerData playerData = dataManager.getPlayerData(playerId);
        if (playerData != null && !playerData.getMailbox().isEmpty()) {
            player.sendMessage(TextUtils.deserializeString("<green>You have " +
                    playerData.getMailbox().size() + " unread messages. Use /mail read to view them."));
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        if (data != null) {
            // Optionally capture the final native flight flag, if desired.
            data.setFlyEnabled(player.getAllowFlight());
            dataManager.savePlayerData(player.getUniqueId());
            dataManager.removePlayerData(player.getUniqueId());
        }
    }
}
