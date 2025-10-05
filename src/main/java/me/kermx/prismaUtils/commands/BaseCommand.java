package me.kermx.prismaUtils.commands;

import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    private final String permission;
    private final boolean allowConsole;
    private final String usage;

    /**
     * @param permission   The permission node required for this command.
     * @param allowConsole Whether console usage is allowed.
     * @param usage        The command usage string (e.g. "/feed [player]").
     */
    public BaseCommand(String permission, boolean allowConsole, String usage) {
        this.permission = permission;
        this.allowConsole = allowConsole;
        this.usage = usage;
    }

    /**
     * @param sender The command sender
     * @param label  The command alias used
     * @param args   The command arguments
     */
    protected abstract boolean onCommandExecute(CommandSender sender, String label, String[] args);

    /**
     * @param sender The command sender
     * @param args   The command arguments
     * @return List of possible completions
     */
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return new ArrayList<>(); // default empty suggestions
    }

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (permission != null && !permission.isEmpty()) {
            if (!sender.hasPermission(permission)) {
                sender.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().noPermissionMessage
                        ));
                return true;
            }
        }

        if (!allowConsole && !(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        boolean success = onCommandExecute(sender, label, args);

        if (!success && usage != null && !usage.isEmpty()) {
            sender.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().incorrectUsageMessage,
                            Placeholder.component("usage", Component.text(command.getUsage()))
                    ));
        }

        return true;
    }

    @Override
    public final List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            return new ArrayList<>();
        }
        return onTabCompleteExecute(sender, args);
    }
}