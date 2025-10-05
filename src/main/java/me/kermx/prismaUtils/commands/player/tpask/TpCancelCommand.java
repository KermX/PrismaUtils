package me.kermx.prismaUtils.commands.player.tpask;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.teleport.TeleportRequest;
import me.kermx.prismaUtils.managers.teleport.TeleportRequestManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class TpCancelCommand extends BaseCommand {
    private final TeleportRequestManager requestManager;

    public TpCancelCommand(TeleportRequestManager requestManager) {
        super("prismautils.command.tpcancel", false, "/tpcancel");
        this.requestManager = requestManager;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        TeleportRequest outgoingRequest = null;
        UUID targetId = null;

        for (UUID target : requestManager.getRequests().keySet()) {
            TeleportRequest request = requestManager.getRequest(target);
            if (request.getSender().equals(player.getUniqueId())) {
                outgoingRequest = request;
                targetId = target;
                break;
            }
        }

        if (outgoingRequest == null) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>You don't have any outgoing teleport requests to cancel."
            ));
            return true;
        }

        Player target = Bukkit.getPlayer(targetId);

        // Notify the target if they're online
        if (target != null && target.isOnline()) {
            target.sendMessage(TextUtils.deserializeString(
                    "<gray>Teleport request from <white><sender> <gray>has been cancelled.",
                    Placeholder.component("sender", player.displayName())
            ));
        }

        // Notify the sender
        player.sendMessage(TextUtils.deserializeString(
                "<green>You cancelled your teleport request to <white><target><green>.",
                Placeholder.component("target", outgoingRequest.getTargetDisplayName())
        ));

        // Remove the request
        requestManager.cancelRequest(targetId);

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return List.of();
    }
}
