package me.kermx.prismaUtils.commands.player.restore;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class BaseRestoreAction extends BaseCommand {

    private final String actionName;
    private final String permissionNodeSelf;
    private final String permissionNodeOthers;
    private final String permissionNodeAll;
    private final String actionMessage;
    private final String actionAllMessage;
    private final String actionOtherMessage;
    private final String actionByOtherMessage;

    public BaseRestoreAction(String actionName, String permissionNodeSelf, String permissionNodeOthers, String permissionNodeAll,
                             String actionMessage, String actionAllMessage, String actionOtherMessage,
                             String actionByOtherMessage) {
        super("prismautils.command." + actionName, true, "/" + actionName + " [player|all]");
        this.actionName = actionName;
        this.permissionNodeSelf = permissionNodeSelf;
        this.permissionNodeOthers = permissionNodeOthers;
        this.permissionNodeAll = permissionNodeAll;
        this.actionMessage = actionMessage;
        this.actionAllMessage = actionAllMessage;
        this.actionOtherMessage = actionOtherMessage;
        this.actionByOtherMessage = actionByOtherMessage;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("You must specify a player name or use \"all\" from the console!");
                return true;
            }
            performAction(player);
            player.sendMessage(TextUtils.deserializeString(actionMessage));
            return true;
        }

        if (args.length == 1) {
            String targetName = args[0];
            if (targetName.equalsIgnoreCase("all")) {
                if (!sender.hasPermission(permissionNodeAll)) {
                    sender.sendMessage(TextUtils.deserializeString(
                            ConfigManager.getInstance().getMessagesConfig().noPermissionMessage)
                    );
                    return true;
                }
                for (Player online : Bukkit.getOnlinePlayers()) {
                    performAction(online);
                }
                sender.sendMessage(TextUtils.deserializeString(actionAllMessage));
                return true;
            }
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                sender.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().playerNotFoundMessage)
                );
                return true;
            }
            if (!sender.hasPermission(permissionNodeOthers)) {
                sender.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().noPermissionMessage)
                );
                return true;
            }
            performAction(target);
            sender.sendMessage(TextUtils.deserializeString(
                    actionOtherMessage, Placeholder.component("target", target.displayName()))
            );
            target.sendMessage(TextUtils.deserializeString(
                    actionByOtherMessage, Placeholder.component("source", sender.name()))
            );
            return true;
        }
        return false;
    }

    protected abstract void performAction(Player player);

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions;
        if (args.length == 1) {
            completions = PlayerUtils.getOnlinePlayerNamesStartingWith(args[0]);
            if ("all".startsWith(args[0].toLowerCase())) {
                completions.add("all");
            }
            return completions;
        }
        return super.onTabCompleteExecute(sender, args);
    }
}