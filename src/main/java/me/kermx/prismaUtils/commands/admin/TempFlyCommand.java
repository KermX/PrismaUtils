package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.feature.FlightManager;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TempFlyCommand extends BaseCommand {

    private final FlightManager flightManager;

    public TempFlyCommand(FlightManager flightManager) {
        super("prismautils.command.tempfly", true, "/tempfly <player> <add/set/check> [seconds]");
        this.flightManager = flightManager;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            return false;
        }

        Player target = PlayerUtils.getOnlinePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(TextUtils.deserializeString(
                    "<red>Player <white>" + args[0] + "</white> not found or not online.</red>"
            ));
            return true;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "add":
                if (args.length < 3) {
                    return false;
                }

                try {
                    long seconds = Long.parseLong(args[2]);
                    if (seconds <= 0) {
                        sender.sendMessage(TextUtils.deserializeString(
                                "<red>Seconds must be a positive number.</red>"
                        ));
                        return true;
                    }

                    flightManager.addTempFlightTime(target, seconds);
                    sender.sendMessage(TextUtils.deserializeString(
                            "<green>Added <white><time></white> seconds of temporary flight to <white><player></white>.</green>",
                            Placeholder.unparsed("time", String.valueOf(seconds)),
                            Placeholder.component("player", target.displayName())
                    ));
                } catch (NumberFormatException e) {
                    sender.sendMessage(TextUtils.deserializeString(
                            "<red>Invalid number: " + args[2] + "</red>"
                    ));
                }
                return true;

            case "set":
                if (args.length < 3) {
                    return false;
                }

                try {
                    long seconds = Long.parseLong(args[2]);
                    if (seconds < 0) {
                        sender.sendMessage(TextUtils.deserializeString(
                                "<red>Seconds must be a non-negative number.</red>"
                        ));
                        return true;
                    }

                    // Get current temp flight and calculate difference
                    long currentTime = flightManager.getRemainingTempFlightTime(target);
                    long difference = seconds - currentTime;

                    if (difference != 0) {
                        flightManager.addTempFlightTime(target, difference);
                    }

                    sender.sendMessage(TextUtils.deserializeString(
                            "<green>Set temporary flight time to <white><time></white> seconds for <white><player></white>.</green>",
                            Placeholder.unparsed("time", String.valueOf(seconds)),
                            Placeholder.component("player", target.displayName())
                    ));
                } catch (NumberFormatException e) {
                    sender.sendMessage(TextUtils.deserializeString(
                            "<red>Invalid number: " + args[2] + "</red>"
                    ));
                }
                return true;

            case "check":
                long remainingTime = flightManager.getRemainingTempFlightTime(target);
                if (remainingTime > 0) {
                    sender.sendMessage(TextUtils.deserializeString(
                            "<green><player></green> has <white><time></white> seconds of temporary flight remaining.",
                            Placeholder.component("player", target.displayName()),
                            Placeholder.unparsed("time", String.valueOf(remainingTime))
                    ));
                } else {
                    sender.sendMessage(TextUtils.deserializeString(
                            "<yellow><player></yellow> has no temporary flight time remaining.",
                            Placeholder.component("player", target.displayName())
                    ));
                }
                return true;

            default:
                return false;
        }
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument: player names
            completions.addAll(PlayerUtils.getOnlinePlayerNamesStartingWith(args[0]));
        } else if (args.length == 2) {
            // Second argument: actions
            String input = args[1].toLowerCase();
            if ("add".startsWith(input)) completions.add("add");
            if ("set".startsWith(input)) completions.add("set");
            if ("check".startsWith(input)) completions.add("check");
        } else if (args.length == 3) {
            // Third argument: seconds (for add/set)
            String action = args[1].toLowerCase();
            if ("add".equals(action) || "set".equals(action)) {
                completions.add("60");
                completions.add("300");
                completions.add("600");
                completions.add("1800");
                completions.add("3600");
            }
        }

        return completions;
    }
}
