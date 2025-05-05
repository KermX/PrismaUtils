package me.kermx.prismaUtils.commands.player.Tpask;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.teleport.TeleportRequest;
import me.kermx.prismaUtils.managers.teleport.TeleportRequestManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TpDenyCommand extends BaseCommand {
    private final TeleportRequestManager requestManager;

    public TpDenyCommand(TeleportRequestManager requestManager) {
        super("prismautils.command.tpdeny", false, "/tpdeny");
        this.requestManager = requestManager;
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

        // Notify requester if they're online
        if (requester != null && requester.isOnline()) {
            requester.sendMessage(TextUtils.deserializeString(
                    "<red><white>" + player.getName() + "<red> denied your teleport request."
            ));
        }

        // Notify the target
        player.sendMessage(TextUtils.deserializeString(
                "<green>You denied the teleport request from <white>" + request.getSenderName() + "<green>."
        ));

        // Remove the request
        requestManager.cancelRequest(player.getUniqueId());

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return List.of();
    }
}
