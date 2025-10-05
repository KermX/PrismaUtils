package me.kermx.prismaUtils.commands.player;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class pTimeCommand extends BaseCommand {

    public pTimeCommand() {
        super("prismautils.command.ptime", false, "/ptime [time|reset|freeze|sync|lock|worldtime]");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("sync")) {
            player.resetPlayerTime();
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().pTimeResetMessage)
            );
            return true;
        }

        if (args[0].equalsIgnoreCase("freeze") || args[0].equalsIgnoreCase("lock") || args[0].equalsIgnoreCase("worldtime")) {
            long worldTime = player.getWorld().getTime();
            player.setPlayerTime(worldTime, true);
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().pTimeSetMessage,
                    Placeholder.component("time", Component.text(worldTime)))
            );
            return true;
        }

        try {
            long time = parseTime(args[0]);
            player.setPlayerTime(time, false);

            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().pTimeSetMessage,
                    Placeholder.component("time", Component.text(time)))
            );
        } catch (IllegalArgumentException e) {
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().pTimeInvalidTimeMessage)
            );
            return false;
        }
        return true;
    }

    private long parseTime(String input) {
        switch (input.toLowerCase()) {
            case "day":
            case "noon":
            case "midday":
                return 6000L;
            case "night":
                return 13000L;
            case "morning":
                return 0L;
            case "sunrise":
            case "dawn":
                return 23000L;
            case "sunset":
            case "dusk":
                return 12500L;
            case "evening":
                return 11000L;
            case "midnight":
                return 18000L;
            default:
                try {
                    long time = Long.parseLong(input);
                    if (time < 0 || time >= 24000) {
                        throw new IllegalArgumentException("Invalid time! Must be between 0 and 24000.");
                    }
                    return time;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid time format! Must be a number between 0 and 24000 or a valid keyword.");
                }
        }
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partialArg = args[0].toLowerCase();
            List<String> options = List.of("day", "noon", "midday", "night", "morning", "sunrise", "sunset", "dusk", "dawn", "evening", "midnight", "reset", "freeze", "lock", "worldtime", "sync");
            for (String option : options) {
                if (option.startsWith(partialArg)) {
                    completions.add(option);
                }
            }
        }
        return completions;
    }
}