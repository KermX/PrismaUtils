package me.kermx.prismaUtils.handlers.block;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.services.ProtectionService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class InvisItemFrameHandler implements Listener {

    private final PrismaUtils plugin;
    private final ProtectionService protectionService;

    public InvisItemFrameHandler(PrismaUtils plugin, ProtectionService protectionService) {
        this.plugin = plugin;
        this.protectionService = protectionService;
    }

    @EventHandler(ignoreCancelled = true)
    public void onShearItemFrame(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof ItemFrame frame)) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (tool.getType() != Material.SHEARS) {
            return;
        }
        if (!frame.isVisible()) {
            return;
        }
        if (frame.getItem().isEmpty()) {
            return;
        }

        Block attachedTo = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());

        if (!protectionService.canBuild(player, attachedTo.getLocation())) {
            return;
        }

        frame.setVisible(false);
        player.playSound(frame.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 0.7f, 1.2f);

        if (player.getGameMode() != GameMode.CREATIVE) {
            tool.damage(1, player);
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemFrameDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame frame)) return;

        if (frame.isVisible()) return;

        if (event.getDamager() instanceof Player player) {
            Block attachedTo = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());


            if (!protectionService.canBuild(player, attachedTo.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (frame.isValid() && frame.getItem().isEmpty()) {
                frame.setVisible(true);
            }
        });
    }
}
