package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SafeSpawnEggHandler implements Listener {

    private static final String BYPASS_PERMISSION = "prismautils.allowspawneggs";

    @EventHandler
    public void onPlayerUseSpawnEgg(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !isSpawnEgg(item.getType())) return;

        if (player.hasPermission(BYPASS_PERMISSION)) return;

        if (player.isSneaking()) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType() == Material.SPAWNER) {
                return;
            }
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(
                TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().spawnEggNotCrouchingMessage));
    }

    private boolean isSpawnEgg(Material material) {
        return material.name().endsWith("_SPAWN_EGG");
    }
}
