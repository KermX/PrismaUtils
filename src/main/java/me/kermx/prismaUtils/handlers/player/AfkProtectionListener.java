package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.managers.features.AfkManager;
import me.kermx.prismaUtils.managers.general.configs.AfkConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class AfkProtectionListener implements Listener {

    private final AfkManager afkManager;
    private final AfkConfigManager afkConfig;

    public AfkProtectionListener(AfkManager afkManager, AfkConfigManager afkConfig) {
        this.afkManager = afkManager;
        this.afkConfig = afkConfig;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!afkConfig.disableAfkDamage) return;

        if (event.getEntity() instanceof Player player) {
            if (afkManager.isAfk(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!afkConfig.disableAfkDamage) return;

        if (event.getTarget() instanceof Player player) {
            if (afkManager.isAfk(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!afkConfig.disableAfkDamage) return;

        Player player = event.getPlayer();
        if (afkManager.isAfk(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}

