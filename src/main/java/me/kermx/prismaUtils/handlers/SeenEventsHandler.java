package me.kermx.prismaUtils.handlers;

import me.kermx.prismaUtils.managers.SeenManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SeenEventsHandler implements Listener {
    private final SeenManager seenManager;

    public SeenEventsHandler(SeenManager seenManager) {
        this.seenManager = seenManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        seenManager.recordLogin(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        seenManager.clearLogin(event.getPlayer().getUniqueId());
    }
}
