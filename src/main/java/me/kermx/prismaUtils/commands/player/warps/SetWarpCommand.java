package me.kermx.prismaUtils.commands.player.warps;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.configs.WarpsConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetWarpCommand extends BaseCommand {
    private final WarpsConfigManager warpsConfigManager;

    public SetWarpCommand(WarpsConfigManager warpsConfigManager) {
        super("prismautils.command.setwarp", false, "/setwarp [name]");
        this.warpsConfigManager = warpsConfigManager;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            return false;
        }

        String warpName = args[0];

        if (!warpName.matches("[a-zA-Z0-9_-]+")) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>Warp names can only contain letters, numbers, underscores, and hyphens."
            ));
            return true;
        }

        Location playerLocation = player.getLocation();
        boolean overwriting = warpsConfigManager.warpExists(warpName);

        warpsConfigManager.setWarp(warpName, playerLocation);

        if (overwriting) {
            player.sendMessage(TextUtils.deserializeString(
                    "<green>Updated warp '<white>" + warpName + "<green>'."
            ));
        } else {
            player.sendMessage(TextUtils.deserializeString(
                    "<green>Created warp '<white>" + warpName + "<green>'."
            ));
        }

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        // No tab completion for setting a new warp name
        return new ArrayList<>();
    }
}
