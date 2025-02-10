package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.features.SeenManager;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SeenCommand extends BaseCommand {

    private final SeenManager seenManager;

    public SeenCommand(SeenManager seenManager){
        super("prismautils.command.seen", true, "/seen <player>");
        this.seenManager = seenManager;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length < 1){
            return false;
        }
        String targetName = args[0];
        Player onlinePlayer = Bukkit.getPlayerExact(targetName);

        if (onlinePlayer != null && onlinePlayer.isOnline()) {
            Long loginTime = seenManager.getLoginTime(onlinePlayer.getUniqueId());
            if (loginTime != null) {
                long sessionMillis = System.currentTimeMillis() - loginTime;
                String duration = TextUtils.formatDuration(sessionMillis);

                sender.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().seenOnlineMessage,
                        Placeholder.component("target", onlinePlayer.displayName()),
                        Placeholder.component("time", Component.text(duration)))
                );
            } else {

                sender.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().seenOnlineMessage,
                        Placeholder.component("target", onlinePlayer.displayName()),
                        Placeholder.component("time", Component.text("Unknown")))
                );
            }
        } else {
            OfflinePlayer offlinePlayer = PlayerUtils.getOfflinePlayer(targetName);
            if (offlinePlayer == null || (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline())) {

                sender.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().seenNeverJoinedMessage,
                        Placeholder.component("target", Component.text(targetName)))
                );

            } else {
                long lastSeen = offlinePlayer.getLastSeen();
                if (lastSeen <= 0) {

                    sender.sendMessage(
                            TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().seenNeverJoinedMessage,
                            Placeholder.component("target", Component.text(targetName)))
                    );

                } else {
                    long timeSinceLastPlayed = System.currentTimeMillis() - lastSeen;
                    String duration = TextUtils.formatDuration(timeSinceLastPlayed);

                    sender.sendMessage(
                            TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().seenOfflineMessage,
                            Placeholder.component("target", Component.text(targetName)),
                            Placeholder.component("time", Component.text(duration)))
                    );
                }
            }
        }
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partial)) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }
}
