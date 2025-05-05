package me.kermx.prismaUtils.commands.player.teleport;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        // Save target's location for /back command
        PlayerData targetData = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
        if (targetData != null) {
            targetData.setLastLocation(target.getLocation().clone());
        }

        // Teleport target to player
        target.teleportAsync(player.getLocation());

        player.sendMessage(TextUtils.deserializeString(
                "<green>Teleported <white>" + target.getName() + "<green> to your location."
        ));

        target.sendMessage(TextUtils.deserializeString(
                "<green>You were teleported to <white>" + player.getName() + "<green>."
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
