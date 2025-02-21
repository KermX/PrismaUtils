package me.kermx.prismaUtils.handlers.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PermissionKeepInvHandler implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("prismautils.keepinventory")) {

            event.setKeepInventory(true);
            event.setKeepLevel(true);

            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }
}
