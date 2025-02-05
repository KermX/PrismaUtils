package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.managers.general.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class HealthScaleHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (ConfigManager.getInstance().healthScaleEnabled) {
            player.setHealthScaled(true);
            player.setHealthScale(ConfigManager.getInstance().healthScaleValue);
        } else {
            player.setHealthScaled(false);
        }
    }
}
