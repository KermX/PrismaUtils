package me.kermx.prismaUtils.commands.player.tpask;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.playerdata.PlayerData;
import me.kermx.prismaUtils.managers.teleport.TeleportRequest;
import me.kermx.prismaUtils.managers.teleport.TeleportRequestManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        // Handle the teleport based on request type
        if (request.getType() == TeleportRequest.Type.TPA) {
            // Save requester's location for /back command
            PlayerData requesterData = plugin.getPlayerDataManager().getPlayerData(requester.getUniqueId());
            if (requesterData != null) {
                requesterData.setLastLocation(requester.getLocation().clone());
            }

            // Teleport requester to target
            requester.teleportAsync(player.getLocation());

            requester.sendMessage(TextUtils.deserializeString(
                    "<green>Teleported to <white><player><green>.",
                    Placeholder.component("player",player.displayName())
            ));

            player.sendMessage(TextUtils.deserializeString(
                    "<white><requester> <green>has been teleported to you.",
                    Placeholder.component("requester", requester.displayName())
            ));
        } else {
            // Save target's location for /back command
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (playerData != null) {
                playerData.setLastLocation(player.getLocation().clone());
            }

            // Teleport target to requester
            player.teleportAsync(requester.getLocation());

            player.sendMessage(TextUtils.deserializeString(
                    "<green>Teleported to <white><requester><green>.",
                    Placeholder.component("requester", requester.displayName())
            ));

            requester.sendMessage(TextUtils.deserializeString(
                    "<white><player><green> has been teleported to you.",
                    Placeholder.component("player", player.displayName())
            ));
        }

        // Remove the request
        requestManager.cancelRequest(player.getUniqueId());

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return List.of();
    }
}
