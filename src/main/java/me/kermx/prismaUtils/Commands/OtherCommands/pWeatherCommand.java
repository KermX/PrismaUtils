package me.kermx.prismaUtils.Commands.OtherCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.WeatherType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class pWeatherCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;


        if (!player.hasPermission("prismautils.command.pweather")){
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("sync")){
            player.resetPlayerWeather();
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().pWeatherResetMessage));
            return true;
        }

        switch (args[0].toLowerCase()){
            case "clear":
                player.setPlayerWeather(WeatherType.CLEAR);
                player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().pWeatherSetMessage,
                        Placeholder.component("weather", Component.text("clear"))));
                break;
            case "rain":
                player.setPlayerWeather(WeatherType.DOWNFALL);
                player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().pWeatherSetMessage,
                        Placeholder.component("weather", Component.text("rain"))));
                break;
            default:
                player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().pWeatherInvalidWeatherMessage));
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1){
            completions.add("clear");
            completions.add("rain");
            completions.add("reset");
        }
        return completions;
    }
}
