package me.kermx.prismaUtils.handlers.block;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Chain;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class ClimbableChainsHandler implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        Block block = player.getLocation().getBlock();

        if (block.getType() != Material.CHAIN) {
            return;
        }

        if (!(block.getBlockData() instanceof Chain chainData) || chainData.getAxis() != Axis.Y) {
            return;
        }

        Vector velocity = player.getVelocity();
        velocity.setY(player.isSneaking() ? -0.2 : 0.2);
        player.setVelocity(velocity);

        player.setFallDistance(0);
    }
}