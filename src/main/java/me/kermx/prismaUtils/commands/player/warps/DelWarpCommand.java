package me.kermx.prismaUtils.commands.player.warps;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.config.WarpsConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DelWarpCommand extends BaseCommand {
    private final WarpsConfigManager warpsConfigManager;


    public DelWarpCommand(WarpsConfigManager warpsConfigManager) {
        super("prismautils.command.delwarp", false, "/delwarp [name]");
        this.warpsConfigManager = warpsConfigManager;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            return false;
        }

        String warpName = args[0];

        if (!warpsConfigManager.warpExists(warpName)) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>Warp [<white>" + warpName + "<red>] doesn't exist."
            ));
            return true;
        }

        warpsConfigManager.deleteWarp(warpName);
        player.sendMessage(TextUtils.deserializeString(
                "<green>Deleted warp [<white>" + warpName + "<green>]."
        ));

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return warpsConfigManager.getWarpNames().stream()
                    .filter(warp -> warp.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
