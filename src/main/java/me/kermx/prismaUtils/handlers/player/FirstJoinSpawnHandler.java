package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.managers.general.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FirstJoinSpawnHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!ConfigManager.getInstance().firstJoinSpawnEnabled) return;

        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) return;

        String worldName = ConfigManager.getInstance().firstJoinSpawnWorld;
        double x = ConfigManager.getInstance().firstJoinSpawnX;
        double y = ConfigManager.getInstance().firstJoinSpawnY;
        double z = ConfigManager.getInstance().firstJoinSpawnZ;
        float yaw = ConfigManager.getInstance().firstJoinSpawnYaw;
        float pitch = ConfigManager.getInstance().firstJoinSpawnPitch;

        Location spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        player.teleport(spawnLocation);
    }
}
