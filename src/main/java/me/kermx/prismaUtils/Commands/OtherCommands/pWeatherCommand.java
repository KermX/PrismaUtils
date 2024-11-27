package me.kermx.prismaUtils.Commands.OtherCommands;

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
            player.sendMessage("You do not have permission to use this command!");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("sync")){
            player.resetPlayerWeather();
            player.sendMessage("Player weather reset to default.");
            return true;
        }

        switch (args[0].toLowerCase()){
            case "clear":
                player.setPlayerWeather(WeatherType.CLEAR);
                player.sendMessage("Player weather set to clear.");
                break;
            case "rain":
                player.setPlayerWeather(WeatherType.DOWNFALL);
                player.sendMessage("Player weather set to rain.");
                break;
            default:
                player.sendMessage("Invalid weather type. Use 'clear', 'rain'");
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
