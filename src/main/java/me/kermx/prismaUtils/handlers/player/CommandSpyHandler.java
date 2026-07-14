package me.kermx.prismaUtils.handlers.player;

import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.managers.feature.CommandSpyManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;

public class CommandSpyHandler implements Listener {

    private static final String SPY_PERMISSION = "prismautils.command.commandspy";

    private final CommandSpyManager commandSpyManager;

    public CommandSpyHandler(CommandSpyManager commandSpyManager) {
        this.commandSpyManager = commandSpyManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (commandSpyManager.getSpyingPlayers().isEmpty()) {
            return;
        }

        Player sender = event.getPlayer();
        UUID senderId = sender.getUniqueId();
        String command = event.getMessage();

        String format = ConfigManager.getInstance().getMessagesConfig().commandSpyFormatMessage;
        if (format == null) {
            return;
        }

        for (UUID watcherId : commandSpyManager.getSpyingPlayers()) {
            if (watcherId.equals(senderId)) {
                continue;
            }
            Player watcher = Bukkit.getPlayer(watcherId);
            if (watcher == null || !watcher.hasPermission(SPY_PERMISSION)) {
                continue;
            }
            watcher.sendMessage(TextUtils.deserializeString(format,
                    Placeholder.component("player", sender.displayName()),
                    Placeholder.unparsed("command", command)));
        }
    }
}