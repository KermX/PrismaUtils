package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayTimeCommand extends BaseCommand {
    public PlayTimeCommand() {
        super("prismautils.command.playtime", true, "/playtime [player]");
    }

    @Override
    protected boolean onCommandExecute (CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                return false;
            }

            Player player = (Player) sender;
            long playTimeInTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
            displayPlayTime(sender, player, playTimeInTicks);
            return true;
        } else {
            String targetName = args[0];
            Player targetPlayer = Bukkit.getPlayer(targetName);

            if (targetPlayer != null && targetPlayer.isOnline()) {
                long playTimeInTicks = targetPlayer.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE);
                displayPlayTime(sender, targetPlayer, playTimeInTicks);
                return true;

            } else {
                OfflinePlayer offlinePlayer = PlayerUtils.getOfflinePlayer(targetName);
                if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                    // We have to load the player data for offline player to get statistics
                    try {
                        long playTimeInTicks = offlinePlayer.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE);
                        displayPlayTime(sender, offlinePlayer, playTimeInTicks);
                    } catch (Exception e) {
                        sender.sendMessage(
                                TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().playerNotFoundMessage,
                                        Placeholder.component("player", Component.text(targetName)))
                        );
                    }
                    return true;
                } else {
                    sender.sendMessage(
                            TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().playerNotFoundMessage,
                                    Placeholder.component("player", Component.text(targetName)))
                    );
                    return false;
                }
            }
        }
    }
    private void displayPlayTime(CommandSender sender, OfflinePlayer player, long playTimeInTicks) {
        // Convert to seconds (20 ticks = 1 second)
        long totalSeconds = playTimeInTicks / 20;

        // Calculate weeks, days, hours, minutes, and seconds
        long weeks = totalSeconds / (7 * 24 * 60 * 60);
        long days = (totalSeconds % (7 * 24 * 60 * 60)) / (24 * 60 * 60);
        long hours = (totalSeconds % (24 * 60 * 60)) / (60 * 60);
        long minutes = (totalSeconds % (60 * 60)) / 60;
        long seconds = totalSeconds % 60;

        // Build a formatted time string
        StringBuilder formattedTime = new StringBuilder();

        if (weeks > 0) {
            formattedTime.append(weeks).append(weeks == 1 ? " week, " : " weeks, ");
        }

        if (days > 0 || weeks > 0) {
            formattedTime.append(days).append(days == 1 ? " day, " : " days, ");
        }

        if (hours > 0 || days > 0 || weeks > 0) {
            formattedTime.append(hours).append(hours == 1 ? " hour, " : " hours, ");
        }

        if (minutes > 0 || hours > 0 || days > 0 || weeks > 0) {
            formattedTime.append(minutes).append(minutes == 1 ? " minute, " : " minutes, ");
        }

        formattedTime.append(seconds).append(seconds == 1 ? " second" : " seconds");

        // Display the message
        Component playerComponent = player instanceof Player ? ((Player) player).displayName() : Component.text(player.getName());
        Component timeComponent = Component.text(formattedTime.toString());

        sender.sendMessage(
                TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().playTimeMessage,
                        Placeholder.component("player", playerComponent),
                        Placeholder.component("playtime", timeComponent))
        );
    }


    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
