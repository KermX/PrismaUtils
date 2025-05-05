package me.kermx.prismaUtils.commands.player.Tpask;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.teleport.TeleportRequest;
import me.kermx.prismaUtils.managers.teleport.TeleportRequestManager;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TpaCommand extends BaseCommand {
    private final TeleportRequestManager requestManager;

    public TpaCommand(TeleportRequestManager requestManager) {
        super("prismautils.command.tpa", false, "/tpa <player>");
        this.requestManager = requestManager;
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
                    "<red>You cannot teleport to yourself."
            ));
            return true;
        }

        // Create and send the teleport request
        TeleportRequest request = new TeleportRequest(
                player.getUniqueId(),
                player.getName(),
                target.getUniqueId(),
                target.getName(),
                TeleportRequest.Type.TPA
        );

        requestManager.sendRequest(request);

        // Notify the requester
        player.sendMessage(TextUtils.deserializeString(
                "<green>Teleport request sent to <white><target><green>.",
                Placeholder.component("target", target.displayName())
        ));

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return PlayerUtils.getOnlinePlayerNamesStartingWith(args[0]);
        }
        return List.of();
    }
}

