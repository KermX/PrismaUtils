package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.managers.core.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FirstJoinSpawnHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!ConfigManager.getInstance().getMainConfig().firstJoinSpawnEnabled) return;

        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) return;

        String worldName = ConfigManager.getInstance().getMainConfig().firstJoinSpawnWorld;
        double x = ConfigManager.getInstance().getMainConfig().firstJoinSpawnX;
        double y = ConfigManager.getInstance().getMainConfig().firstJoinSpawnY;
        double z = ConfigManager.getInstance().getMainConfig().firstJoinSpawnZ;
        float yaw = ConfigManager.getInstance().getMainConfig().firstJoinSpawnYaw;
        float pitch = ConfigManager.getInstance().getMainConfig().firstJoinSpawnPitch;

        Location spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        player.teleport(spawnLocation);
    }
}