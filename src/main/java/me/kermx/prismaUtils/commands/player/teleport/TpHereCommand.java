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

public class TpHereCommand extends BaseCommand {

    private final PrismaUtils plugin;

    public TpHereCommand(PrismaUtils plugin) {
        super("prismautils.command.tphere", false, "/tphere <player>");
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

        // Can't teleport yourself to yourself
        if (target.equals(player)) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>You cannot teleport yourself to yourself."
            ));
            return true;
        }

        if (!TeleportUtils.tryBeginTeleport(target)) {
            player.sendMessage(TextUtils.deserializeString("<red>That player is already teleporting. Please wait.</red>"));
            return true;
        }

        boolean teleportScheduled = false;
        try {
            // Save target's location for /back command (only commit on successful teleport)
            PlayerData targetData = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
            Location targetCurrentLocation = target.getLocation().clone();
            Location destination = player.getLocation().clone();

            teleportScheduled = true;
            TeleportUtils.teleportAsyncWithChunkReady(plugin, target, destination, PlayerTeleportEvent.TeleportCause.PLUGIN)
                    .whenComplete((success, throwable) -> {
                        TeleportUtils.endTeleport(target);

                        if (!target.isOnline()) {
                            return;
                        }

                        if (throwable != null || !Boolean.TRUE.equals(success)) {
                            target.sendMessage(TextUtils.deserializeString("<red>Teleport failed. Try again in a moment.</red>"));
                            if (player.isOnline()) {
                                player.sendMessage(TextUtils.deserializeString("<red>Teleport failed. Try again in a moment.</red>"));
                            }
                            return;
                        }

                        if (targetData != null) {
                            targetData.setLastLocation(targetCurrentLocation);
                        }

                        if (player.isOnline()) {
                            player.sendMessage(TextUtils.deserializeString(
                                    "<green>Teleported <white>" + target.getName() + "<green> to your location."
                            ));
                        }

                        target.sendMessage(TextUtils.deserializeString(
                                "<green>You were teleported to <white>" + player.getName() + "<green>."
                        ));
                    });

            return true;
        } finally {
            if (!teleportScheduled) {
                TeleportUtils.endTeleport(target);
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