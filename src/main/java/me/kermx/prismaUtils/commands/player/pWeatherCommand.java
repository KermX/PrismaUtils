package me.kermx.prismaUtils.commands.player;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class pWeatherCommand extends BaseCommand {

    public pWeatherCommand() {
        super("prismautils.command.pweather", false, "/pweather");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("sync")) {
            Player player = (Player) sender;
            player.resetPlayerWeather();
            player.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().pWeatherResetMessage)
            );
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "clear":
                ((Player) sender).setPlayerWeather(WeatherType.CLEAR);
                sender.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().pWeatherSetMessage,
                        Placeholder.component("weather", Component.text("clear")))
                );
                break;
            case "rain":
                ((Player) sender).setPlayerWeather(WeatherType.DOWNFALL);
                sender.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().pWeatherSetMessage,
                        Placeholder.component("weather", Component.text("rain")))
                );
                break;
            default:
                sender.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().pWeatherInvalidWeatherMessage)
                );
                break;
        }
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("clear");
            completions.add("rain");
            completions.add("reset");
        }
        return completions;
    }
}
