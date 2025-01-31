package me.kermx.prismaUtils.Commands.OtherCommands;

import me.kermx.prismaUtils.Managers.SeenManager;
import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SeenCommand implements CommandExecutor {

    private final SeenManager seenManager;

    public SeenCommand(SeenManager seenManager) {
        this.seenManager = seenManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1){
            sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().incorrectUsageMessage,
                    Placeholder.component("usage", Component.text(command.getUsage()))));
            return true;
        }

        if (!sender.hasPermission("prismautils.command.seen")){
            sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            return true;
        }

        String targetName = args[0];
        Player onlinePlayer = Bukkit.getPlayerExact(targetName);
        if (onlinePlayer != null && onlinePlayer.isOnline()){
            Long loginTime = seenManager.getLoginTime(onlinePlayer.getUniqueId());
            if (loginTime != null){
                long sessionMillis = System.currentTimeMillis() - loginTime;
                String duration = seenManager.formatDuration(sessionMillis);
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().seenOnlineMessage,
                        Placeholder.component("target", onlinePlayer.displayName()),
                        Placeholder.component("time", Component.text(duration))));
            } else {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().seenOnlineMessage,
                        Placeholder.component("target", onlinePlayer.displayName()),
                        Placeholder.component("time", Component.text("Unknown"))));
            }
        } else {
            OfflinePlayer offlinePlayer = seenManager.getOfflinePlayer(targetName);
            if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()){
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().seenNeverJoinedMessage,
                        Placeholder.component("target", Component.text(targetName))));
            } else {
                long lastSeen = offlinePlayer.getLastSeen();
                if (lastSeen <= 0){
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().seenNeverJoinedMessage,
                            Placeholder.component("target", Component.text(targetName))));
                } else {
                    long timeSinceLastPlayed = System.currentTimeMillis() - lastSeen;
                    String duration = seenManager.formatDuration(timeSinceLastPlayed);
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().seenOfflineMessage,
                            Placeholder.component("target", Component.text(targetName)),
                            Placeholder.component("time", Component.text(duration))));
                }
            }
        }
        return true;
    }
}
