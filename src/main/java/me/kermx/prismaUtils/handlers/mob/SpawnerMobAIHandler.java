package me.kermx.prismaUtils.handlers.mob;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SpawnerMobAIHandler implements Listener {

    private final List<String> triggerNames;

    public SpawnerMobAIHandler(List<String> triggerNames) {
        this.triggerNames = triggerNames;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity entity)) return;

        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (item.getType() != Material.NAME_TAG) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        net.kyori.adventure.text.Component displayName = meta.displayName();
        if (displayName == null) return;

        String name = PlainTextComponentSerializer.plainText().serialize(displayName);
        if (!triggerNames.contains(name)) return;

        entity.setAI(true);
    }
}