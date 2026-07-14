package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.managers.feature.AfkManager;
import me.kermx.prismaUtils.managers.config.AfkConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class AfkProtectionHandler implements Listener {

    private final AfkManager afkManager;
    private final AfkConfigManager afkConfig;

    public AfkProtectionHandler(AfkManager afkManager, AfkConfigManager afkConfig) {
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (shouldBlockTeleport(event.getPlayer(), event.getCause())) {
            event.setCancelled(true);
        }
    }

    /**
     * PlayerPortalEvent extends PlayerTeleportEvent but has its own handler list,
     * so portal teleports never reach onPlayerTeleport and need their own handler.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (shouldBlockTeleport(event.getPlayer(), event.getCause())) {
            event.setCancelled(true);
        }
    }

    private boolean shouldBlockTeleport(Player player, PlayerTeleportEvent.TeleportCause cause) {
        if (!afkConfig.blockedTeleportCauses.contains(cause)) return false;
        if (!afkManager.isAfk(player.getUniqueId())) return false;

        // Never block the AFK system's own to/from AFK-area teleports
        return !afkManager.isSystemTeleporting(player.getUniqueId());
    }
}

