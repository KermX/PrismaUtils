package me.kermx.prismaUtils.Commands.OtherCommands;

import me.kermx.prismaUtils.Commands.base.BaseCommand;
import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class pWeatherCommand extends BaseCommand {

    public pWeatherCommand(){
        super("prismautils.command.pweather", false, "/pweather");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length == 0 || args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("sync")) {
            Player player = (Player) sender;
            player.resetPlayerWeather();
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().pWeatherResetMessage));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "clear":
                ((Player) sender).setPlayerWeather(WeatherType.CLEAR);
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().pWeatherSetMessage,
                        Placeholder.component("weather", Component.text("clear"))));
                break;
            case "rain":
                ((Player) sender).setPlayerWeather(WeatherType.DOWNFALL);
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().pWeatherSetMessage,
                        Placeholder.component("weather", Component.text("rain"))));
                break;
            default:
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().pWeatherInvalidWeatherMessage));
                break;
        }
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("clear");
            completions.add("rain");
            completions.add("reset");
        }
        return completions;
    }
}
