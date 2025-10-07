package me.kermx.prismaUtils.commands.player.teleport;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.playerdata.PlayerData;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        // Save player's location for /back command
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (playerData != null) {
            playerData.setLastLocation(player.getLocation().clone());
        }

        // Teleport player to target
        player.teleportAsync(target.getLocation());

        player.sendMessage(TextUtils.deserializeString(
                "<green>Teleported to <white>" + target.getName() + "<green>."
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

