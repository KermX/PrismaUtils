package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NearCommand extends BaseCommand {

    public NearCommand() {
        super("prismautils.command.near", false, "/near [radius]");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        Location location = player.getLocation();
        double radius = 256; // Default radius

        if (args.length > 0) {
            try {
                radius = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().nearInvalidRadiusMessage)
                );
                return false;
            }
        }

        Component radiusComponent = Component.text(radius);
        sender.sendMessage(
                TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().nearNearPlayersMessage,
                        Placeholder.component("radius", radiusComponent))
        );

        boolean found = false;

        for (Player onlinePlayer : player.getWorld().getPlayers()) {
            if (onlinePlayer.getLocation().distance(location) <= radius && !onlinePlayer.equals(player)) {
                sender.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().nearNearbyPlayersMessage,
                                Placeholder.component("player", onlinePlayer.displayName()),
                                Placeholder.component("distance", Component.text(onlinePlayer.getLocation().distance(location))))
                );
                found = true;
            }
        }

        if (!found) {
            sender.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().nearNoPlayersMessage,
                            Placeholder.component("radius", radiusComponent))
            );
        }

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partialArg = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            List<String> options = List.of("32", "64", "128", "256", "512");

            for (String option : options) {
                if (option.startsWith(partialArg)) {
                    suggestions.add(option);
                }
            }

            return suggestions;
        }

        return super.onTabCompleteExecute(sender, args);
    }
}