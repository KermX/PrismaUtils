package me.kermx.prismaUtils.commands.player.teleport;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.playerdata.PlayerData;
import me.kermx.prismaUtils.managers.playerdata.PlayerDataManager;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.managers.core.CooldownManager;
import me.kermx.prismaUtils.utils.TeleportUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BackCommand extends BaseCommand {
    private final PrismaUtils plugin;
    private final PlayerDataManager playerDataManager;

    public BackCommand(PrismaUtils plugin, PlayerDataManager playerDataManager) {
        super("prismautils.command.back", false, "/back");
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (!TeleportUtils.tryBeginTeleport(player)) {
            player.sendMessage(TextUtils.deserializeString("<red>Teleport already in progress. Please wait.</red>"));
            return true;
        }

        boolean teleportScheduled = false;
        try {
            PlayerData playerData = playerDataManager.getPlayerData(uuid);
            Location lastLocation = playerData.getLastLocation();

            if (lastLocation == null) {
                player.sendMessage(TextUtils.deserializeString(
                        "<red>You don't have a previous location to return to."
                ));
                return true;
            }

            World targetWorld = lastLocation.getWorld();
            if (targetWorld == null) {
                player.sendMessage(TextUtils.deserializeString(
                        "<red>Your previous location's world is missing/unloaded."
                ));
                playerData.setLastLocation(null);
                return true;
            }

            if (!Double.isFinite(lastLocation.getX())
                    || !Double.isFinite(lastLocation.getY())
                    || !Double.isFinite(lastLocation.getZ())
                    || !Float.isFinite(lastLocation.getYaw())
                    || !Float.isFinite(lastLocation.getPitch())) {
                player.sendMessage(TextUtils.deserializeString(
                        "<red>Your previous location is invalid."
                ));
                playerData.setLastLocation(null);
                return true;
            }

            int minY = targetWorld.getMinHeight();
            int maxY = targetWorld.getMaxHeight();
            if (lastLocation.getY() < (minY - 64) || lastLocation.getY() > (maxY + 64)) {
                player.sendMessage(TextUtils.deserializeString(
                        "<red>Your previous location is out of bounds."
                ));
                return true;
            }

            if (!ConfigManager.getInstance().getMainConfig().backWhitelistedWorlds.contains(targetWorld.getName())) {
                player.sendMessage(TextUtils.deserializeString(
                        "<red>You can't return to that world."
                ));
                return true;
            }

            CooldownManager cooldownManager = CooldownManager.getInstance();
            if (!cooldownManager.canUseBackCommand(player)) {
                int remainingSeconds = cooldownManager.getBackCooldownRemaining(player);
                player.sendMessage(TextUtils.deserializeString(
                        "<red>You must wait <white>" + remainingSeconds + " second" + (remainingSeconds == 1 ? "" : "s")
                                + "<red> before using this command again."
                ));
                return true;
            }

            Location currentLocation = player.getLocation().clone();
            cooldownManager.setBackCommandCooldown(player);

            teleportScheduled = true;
            TeleportUtils.teleportAsyncWithChunkReady(plugin, player, lastLocation, PlayerTeleportEvent.TeleportCause.PLUGIN)
                    .whenComplete((success, throwable) -> {
                        TeleportUtils.endTeleport(player);

                        if (!player.isOnline()) {
                            return;
                        }

                        if (throwable != null || !Boolean.TRUE.equals(success)) {
                            player.sendMessage(TextUtils.deserializeString("<red>That area isn't ready yet. Try again in a moment.</red>"));
                            return;
                        }

                        player.sendMessage(TextUtils.deserializeString(
                                "<green>Teleported to your previous location."
                        ));

                        playerData.setLastLocation(currentLocation);
                    });

            return true;
        } finally {
            if (!teleportScheduled) {
                TeleportUtils.endTeleport(player);
            }
        }
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        // No tab completion needed for this command
        return super.onTabCompleteExecute(sender, args);
    }
}