package me.kermx.prismaUtils.Handlers;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FirstJoinSpawnHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!ConfigUtils.getInstance().firstJoinSpawnEnabled) return;

        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) return;

        String worldName = ConfigUtils.getInstance().firstJoinSpawnWorld;
        double x = ConfigUtils.getInstance().firstJoinSpawnX;
        double y = ConfigUtils.getInstance().firstJoinSpawnY;
        double z = ConfigUtils.getInstance().firstJoinSpawnZ;
        float yaw = ConfigUtils.getInstance().firstJoinSpawnYaw;
        float pitch = ConfigUtils.getInstance().firstJoinSpawnPitch;

        Location spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        player.teleport(spawnLocation);
    }
}
