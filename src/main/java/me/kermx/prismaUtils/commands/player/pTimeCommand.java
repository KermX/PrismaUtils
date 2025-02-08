package me.kermx.prismaUtils.commands.player;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class pTimeCommand extends BaseCommand {

    public pTimeCommand(){
        super("prismautils.command.ptime", false, "/ptime [time|reset]");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length == 0 || args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("sync")) {
            ((Player) sender).resetPlayerTime();
            sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().pTimeResetMessage));
            return true;
        }

        try {
            long time = parseTime(args[0]);
            ((Player) sender).setPlayerTime(time, false);

            sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().pTimeSetMessage,
                    Placeholder.component("time", Component.text(time))));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().pTimeInvalidTimeMessage));
            return false;
        }
        return true;
    }

    private long parseTime(String input) {
        switch (input.toLowerCase()) {
            case "day", "noon", "midday":
                return 6000L;
            case "night":
                return 13000L;
            case "morning":
                return 0L;
            case "sunrise":
                return 23000L;
            case "sunset":
                return 12500L;
            case "evening":
                return 11000L;
            case "midnight":
                return 18000L;
            default:
                long time = Long.parseLong(input);
                if (time < 0 || time > 24000) {
                    throw new IllegalArgumentException("Invalid time! Must be between 0 and 24000.");
                }
                return time;
        }
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("day");
            completions.add("noon");
            completions.add("midday");
            completions.add("night");
            completions.add("morning");
            completions.add("sunrise");
            completions.add("sunset");
            completions.add("evening");
            completions.add("midnight");
            completions.add("reset");
        }
        return completions;
    }

}
