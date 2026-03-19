package me.kermx.prismaUtils.commands.player.teleport;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.playerdata.PlayerData;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TeleportUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class TpCommand extends BaseCommand {

    private final PrismaUtils plugin;

    public TpCommand(PrismaUtils plugin) {
        super("prismautils.command.tp", false, "/tp <player>");
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            return false;
        }

        String targetName = args[0];
        Player target = PlayerUtils.getOnlinePlayer(targetName);

        if (target == null) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>Player <white>" + targetName + "<red> not found or not online."
            ));
            return true;
        }

        // Can't teleport to yourself
        if (target.equals(player)) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>You are already at your own location."
            ));
            return true;
        }

        if (!TeleportUtils.tryBeginTeleport(player)) {
            player.sendMessage(TextUtils.deserializeString("<red>Teleport already in progress. Please wait.</red>"));
            return true;
        }

        boolean teleportScheduled = false;
        try {
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            Location currentLocation = player.getLocation().clone();
            Location destination = target.getLocation().clone();

            teleportScheduled = true;
            TeleportUtils.teleportAsyncWithChunkReady(plugin, player, destination, PlayerTeleportEvent.TeleportCause.PLUGIN)
                    .whenComplete((success, throwable) -> {
                        TeleportUtils.endTeleport(player);

                        if (!player.isOnline()) {
                            return;
                        }

                        if (throwable != null || !Boolean.TRUE.equals(success)) {
                            player.sendMessage(TextUtils.deserializeString("<red>Teleport failed. Try again in a moment.</red>"));
                            return;
                        }

                        if (playerData != null) {
                            playerData.setLastLocation(currentLocation);
                        }

                        player.sendMessage(TextUtils.deserializeString(
                                "<green>Teleported to <white>" + target.getName() + "<green>."
                        ));
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
        if (args.length == 1) {
            return PlayerUtils.getOnlinePlayerNamesStartingWith(args[0]);
        }
        return List.of();
    }
}