package me.kermx.prismaUtils.commands.player.teleport;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.playerdata.PlayerData;
import me.kermx.prismaUtils.managers.playerdata.PlayerDataManager;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.managers.core.CooldownManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class BackCommand extends BaseCommand {
    private final PlayerDataManager playerDataManager;

    public BackCommand(PlayerDataManager playerDataManager) {
        super("prismautils.command.back", false, "/back");
        this.playerDataManager = playerDataManager;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
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

        if (!ConfigManager.getInstance().getMainConfig().backWhitelistedWorlds.contains(lastLocation.getWorld().getName())) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>You can't return to that world."
            ));
            return true;
        }

        Chunk targetChunk = lastLocation.getChunk();
        if (!targetChunk.isLoaded()) {
            boolean loaded = targetChunk.load(true);
            if (!loaded) {
                player.sendMessage(TextUtils.deserializeString(
                        "<red>That area isn't ready yet. Try again in a moment."
                ));
                return true;
            }
        }

        CooldownManager cooldownManager = CooldownManager.getInstance();
        if (!cooldownManager.canUseBackCommand(player)) {
            int remainingSeconds = cooldownManager.getBackCooldownRemaining(player);
            player.sendMessage(TextUtils.deserializeString("<red>You must wait <white>" + remainingSeconds + " second" + (remainingSeconds == 1 ? "" : "s") + "<red> before using this command again."));
            return true;
        }

        Location currentLocation = player.getLocation().clone();

        // Apply cooldown
        cooldownManager.setBackCommandCooldown(player);

        // Teleport the player (only update state / message if it actually succeeds)
        player.teleportAsync(lastLocation, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(success -> {
            if (!player.isOnline()) {
                return;
            }
            if (!success) {
                player.sendMessage(TextUtils.deserializeString("<red>Teleport failed. Try again in a moment.</red>"));
                return;
            }

            player.sendMessage(TextUtils.deserializeString(
                    "<green>Teleported to your previous location."
            ));

            // Set the current location as the new last location
            playerData.setLastLocation(currentLocation);
        });

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        // No tab completion needed for this command
        return super.onTabCompleteExecute(sender, args);
    }
}
