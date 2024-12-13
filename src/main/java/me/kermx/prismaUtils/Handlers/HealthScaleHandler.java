package me.kermx.prismaUtils.Handlers;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class HealthScaleHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (ConfigUtils.getInstance().healthScaleEnabled) {
            player.setHealthScaled(true);
            player.setHealthScale(ConfigUtils.getInstance().healthScaleValue);
        } else {
            player.setHealthScaled(false);
        }
    }
}
