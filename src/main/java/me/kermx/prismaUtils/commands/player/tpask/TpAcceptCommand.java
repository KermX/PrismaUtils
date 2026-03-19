package me.kermx.prismaUtils.commands.player.tpask;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.playerdata.PlayerData;
import me.kermx.prismaUtils.managers.teleport.TeleportRequest;
import me.kermx.prismaUtils.managers.teleport.TeleportRequestManager;
import me.kermx.prismaUtils.utils.TeleportUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class TpAcceptCommand extends BaseCommand {
    private final TeleportRequestManager requestManager;
    private final PrismaUtils plugin;

    public TpAcceptCommand(TeleportRequestManager requestManager, PrismaUtils plugin) {
        super("prismautils.command.tpaccept", false, "/tpaccept");
        this.requestManager = requestManager;
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        if (!requestManager.hasActiveRequest(player.getUniqueId())) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>You don't have any pending teleport requests."
            ));
            return true;
        }

        TeleportRequest request = requestManager.getRequest(player.getUniqueId());
        Player requester = Bukkit.getPlayer(request.getSender());

        if (requester == null || !requester.isOnline()) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>The player who sent the request is no longer online."
            ));
            requestManager.cancelRequest(player.getUniqueId());
            return true;
        }

        // Cancel immediately so it can't be accepted twice while the teleport is in-flight
        requestManager.cancelRequest(player.getUniqueId());

        if (request.getType() == TeleportRequest.Type.TPA) {
            // requester -> player
            if (!TeleportUtils.tryBeginTeleport(requester)) {
                player.sendMessage(TextUtils.deserializeString("<red>That player is already teleporting. Please wait.</red>"));
                return true;
            }

            boolean teleportScheduled = false;
            try {
                PlayerData requesterData = plugin.getPlayerDataManager().getPlayerData(requester.getUniqueId());
                Location requesterCurrentLocation = requester.getLocation().clone();
                Location destination = player.getLocation().clone();

                teleportScheduled = true;
                TeleportUtils.teleportAsyncWithChunkReady(plugin, requester, destination, PlayerTeleportEvent.TeleportCause.PLUGIN)
                        .whenComplete((success, throwable) -> {
                            TeleportUtils.endTeleport(requester);

                            if (!requester.isOnline()) {
                                return;
                            }

                            if (throwable != null || !Boolean.TRUE.equals(success)) {
                                requester.sendMessage(TextUtils.deserializeString("<red>Teleport failed. Try again in a moment.</red>"));
                                if (player.isOnline()) {
                                    player.sendMessage(TextUtils.deserializeString("<red>Teleport failed. Try again in a moment.</red>"));
                                }
                                return;
                            }

                            if (requesterData != null) {
                                requesterData.setLastLocation(requesterCurrentLocation);
                            }

                            requester.sendMessage(TextUtils.deserializeString(
                                    "<green>Teleported to <white><player><green>.",
                                    Placeholder.component("player", player.displayName())
                            ));

                            if (player.isOnline()) {
                                player.sendMessage(TextUtils.deserializeString(
                                        "<white><requester> <green>has been teleported to you.",
                                        Placeholder.component("requester", requester.displayName())
                                ));
                            }
                        });

                return true;
            } finally {
                if (!teleportScheduled) {
                    TeleportUtils.endTeleport(requester);
                }
            }
        } else {
            // player -> requester (TPAHERE)
            if (!TeleportUtils.tryBeginTeleport(player)) {
                player.sendMessage(TextUtils.deserializeString("<red>Teleport already in progress. Please wait.</red>"));
                return true;
            }

            boolean teleportScheduled = false;
            try {
                PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                Location playerCurrentLocation = player.getLocation().clone();
                Location destination = requester.getLocation().clone();

                teleportScheduled = true;
                TeleportUtils.teleportAsyncWithChunkReady(plugin, player, destination, PlayerTeleportEvent.TeleportCause.PLUGIN)
                        .whenComplete((success, throwable) -> {
                            TeleportUtils.endTeleport(player);

                            if (!player.isOnline()) {
                                return;
                            }

                            if (throwable != null || !Boolean.TRUE.equals(success)) {
                                player.sendMessage(TextUtils.deserializeString("<red>Teleport failed. Try again in a moment.</red>"));
                                if (requester.isOnline()) {
                                    requester.sendMessage(TextUtils.deserializeString("<red>Teleport failed. Try again in a moment.</red>"));
                                }
                                return;
                            }

                            if (playerData != null) {
                                playerData.setLastLocation(playerCurrentLocation);
                            }

                            player.sendMessage(TextUtils.deserializeString(
                                    "<green>Teleported to <white><requester><green>.",
                                    Placeholder.component("requester", requester.displayName())
                            ));

                            if (requester.isOnline()) {
                                requester.sendMessage(TextUtils.deserializeString(
                                        "<white><player><green> has been teleported to you.",
                                        Placeholder.component("player", player.displayName())
                                ));
                            }
                        });

                return true;
            } finally {
                if (!teleportScheduled) {
                    TeleportUtils.endTeleport(player);
                }
            }
        }
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return List.of();
    }
}