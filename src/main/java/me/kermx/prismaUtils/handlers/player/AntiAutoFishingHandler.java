package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiAutoFishingHandler implements Listener {

    private final PrismaUtils plugin;
    private final Map<UUID, Map<String, Integer>> playerFishCounts = new HashMap<>();
    private final Map<UUID, Location> lastFishingLocations = new HashMap<>();

    private final int maxFishPerArea;
    private final double areaRadius;
    private final double requiredMoveDistance;
    private final boolean enabled;

    public AntiAutoFishingHandler(PrismaUtils plugin) {
        this.plugin = plugin;

        // Load settings from config (or use defaults for now)
        this.maxFishPerArea = 30; //ConfigManager.getInstance().getMainConfig().antiFishingMaxFish;
        this.areaRadius = 4; //ConfigManager.getInstance().getMainConfig().antiFishingAreaRadius;
        this.requiredMoveDistance = 5; //ConfigManager.getInstance().getMainConfig().antiFishingRequiredMoveDistance;
        this.enabled = true; //ConfigManager.getInstance().getMainConfig().antiFishingEnabled;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerFish(PlayerFishEvent event) {
        if (!enabled) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        FishHook hook = event.getHook();
        Location bobberLocation = hook.getLocation();

        String areaKey = getAreaKey(bobberLocation);

        playerFishCounts.putIfAbsent(playerId, new HashMap<>());
        Map<String, Integer> playerAreas = playerFishCounts.get(playerId);

        playerAreas.putIfAbsent(areaKey, 0);

        if (lastFishingLocations.containsKey(playerId)) {
            Location lastLocation = lastFishingLocations.get(playerId);

            // Check if the worlds are the same before calculating distance
            if (lastLocation.getWorld().equals(bobberLocation.getWorld())) {
                double distance = lastLocation.distance(bobberLocation);

                if (distance < requiredMoveDistance && playerAreas.get(areaKey) >= maxFishPerArea) {
                    event.setCancelled(true);

                    Component message = TextUtils.deserializeString(
                            "<green>Looks like you've caught too many fish in this area! Move at least " + requiredMoveDistance + " blocks away to continue fishing."
                    );
                    player.sendMessage(message);
                    return;
                }

                if (distance > requiredMoveDistance) {
                    playerAreas.put(areaKey, 0);
                }
            } else {
                // They've changed worlds, so reset the counter for this area
                playerAreas.put(areaKey, 0);
            }
        }

        int newCount = playerAreas.get(areaKey) + 1;
        playerAreas.put(areaKey, newCount);

        lastFishingLocations.put(playerId, bobberLocation);

        // Warn player when approaching the limit
        if (newCount == maxFishPerArea - 1) {
            Component warningMessage = TextUtils.deserializeString(
                    "<yellow>You've almost reached the fishing limit for this area! " +
                            "Move at least " + requiredMoveDistance + " blocks away to continue fishing."
            );
            player.sendMessage(warningMessage);
        }

        // Notify player when they've reached the limit
        if (newCount == maxFishPerArea) {
            Component limitMessage = TextUtils.deserializeString(
                    "<red>You've reached the fishing limit for this area! " +
                            "Your next catch here will be blocked. Please move at least " +
                            requiredMoveDistance + " blocks away to continue fishing."
            );
            player.sendMessage(limitMessage);
        }
    }

    private String getAreaKey(Location location) {
        // Round to the nearest area radius to define "fishing areas"
        int areaX = (int) Math.round(location.getX() / areaRadius);
        int areaY = (int) Math.round(location.getY() / areaRadius);
        int areaZ = (int) Math.round(location.getZ() / areaRadius);
        return location.getWorld().getName() + ":" + areaX + "," + areaY + "," + areaZ;
    }


}
