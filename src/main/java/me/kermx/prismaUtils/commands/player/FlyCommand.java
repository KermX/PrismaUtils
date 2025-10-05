package me.kermx.prismaUtils.commands.player;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.feature.FlightManager;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FlyCommand extends BaseCommand {

    private final FlightManager flightManager;

    public FlyCommand(FlightManager flightManager) {
        super("prismautils.command.fly", false, "/fly [player] [on/off]");
        this.flightManager = flightManager;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        // No arguments - toggle own flight
        if (args.length == 0) {
            flightManager.toggleFlight(player);
            return true;
        }

        // One argument - could be "on/off" or player name
        if (args.length == 1) {
            String arg = args[0].toLowerCase();

            // Check if it's on/off for self
            if (arg.equals("on") || arg.equals("off")) {
                boolean enable = arg.equals("on");
                flightManager.setFlightEnabled(player, enable);
                return true;
            }

            // Must be a player name - check admin permission
            if (!player.hasPermission("prismautils.command.fly.others")) {
                player.sendMessage(TextUtils.deserializeString(
                        "<red>You don't have permission to toggle flight for other players.</red>"
                ));
                return true;
            }

            Player target = PlayerUtils.getOnlinePlayer(arg);
            if (target == null) {
                player.sendMessage(TextUtils.deserializeString(
                        "<red>Player <white>" + arg + "</white> not found or not online.</red>"
                ));
                return true;
            }

            flightManager.toggleFlight(target);
            player.sendMessage(TextUtils.deserializeString(
                    "<green>Toggled flight for <white><player></white>.</green>",
                    Placeholder.component("player", target.displayName())
            ));
            return true;
        }

        // Two arguments - player and on/off
        if (args.length == 2) {
            if (!player.hasPermission("prismautils.command.fly.others")) {
                player.sendMessage(TextUtils.deserializeString(
                        "<red>You don't have permission to toggle flight for other players.</red>"
                ));
                return true;
            }

            Player target = PlayerUtils.getOnlinePlayer(args[0]);
            if (target == null) {
                player.sendMessage(TextUtils.deserializeString(
                        "<red>Player <white>" + args[0] + "</white> not found or not online.</red>"
                ));
                return true;
            }

            String state = args[1].toLowerCase();
            if (!state.equals("on") && !state.equals("off")) {
                return false; // Show usage
            }

            boolean enable = state.equals("on");
            flightManager.setFlightEnabled(target, enable);

            player.sendMessage(TextUtils.deserializeString(
                    "<green>Set flight to <white>" + (enable ? "enabled" : "disabled") + "</white> for <white><player></white>.</green>",
                    Placeholder.component("player", target.displayName())
            ));
            return true;
        }

        return false; // Show usage
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument: player names or on/off
            completions.addAll(PlayerUtils.getOnlinePlayerNamesStartingWith(args[0]));

            if ("on".startsWith(args[0].toLowerCase())) {
                completions.add("on");
            }
            if ("off".startsWith(args[0].toLowerCase())) {
                completions.add("off");
            }
        } else if (args.length == 2) {
            // Second argument: on/off
            if ("on".startsWith(args[1].toLowerCase())) {
                completions.add("on");
            }
            if ("off".startsWith(args[1].toLowerCase())) {
                completions.add("off");
            }
        }

        return completions;
    }
}

