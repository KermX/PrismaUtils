package me.kermx.prismaUtils.Commands.OtherCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FlySpeedCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("prismautils.command.flyspeed")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().incorrectUsageMessage,
                    Placeholder.component("usage", Component.text(command.getUsage()))));
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            player.setFlySpeed(0.1f);
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().flyspeedResetMessage));
            return true;
        }

        try {
            float speed = Float.parseFloat(args[0]);
            if (speed < 0 || speed > 10) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().flyspeedInvalidSpeedMessage));
                return true;
            }
            float adjustedSpeed = speed / 10.0f;
            player.setFlySpeed(adjustedSpeed);

            Component speedComponent = Component.text(speed);
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().flyspeedSetMessage,
                    Placeholder.component("speed", speedComponent)));

            //player.sendMessage("Fly speed set to " + speed + ".");

        } catch (NumberFormatException e) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().flyspeedInvalidSpeedMessage));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partialArg = args[0].toLowerCase();

            // Suggest "reset" if it matches the partial argument
            if ("reset".startsWith(partialArg)) {
                completions.add("reset");
            }

            // Suggest numbers from 0 to 10 that start with the partial argument
            for (int i = 0; i <= 10; i++) {
                String speedStr = String.valueOf(i);
                if (speedStr.startsWith(partialArg)) {
                    completions.add(speedStr);
                }
            }
        }
        return completions;
    }
}
