package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.managers.feature.CommandSpyManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSpyCommand extends BaseCommand {

    private final CommandSpyManager commandSpyManager;

    public CommandSpyCommand(CommandSpyManager commandSpyManager) {
        super("prismautils.command.commandspy", false, "/commandspy");
        this.commandSpyManager = commandSpyManager;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            return false;
        }

        Player player = (Player) sender;
        boolean enabled = commandSpyManager.toggle(player.getUniqueId());

        String message = enabled
                ? ConfigManager.getInstance().getMessagesConfig().commandSpyEnabledMessage
                : ConfigManager.getInstance().getMessagesConfig().commandSpyDisabledMessage;
        player.sendMessage(TextUtils.deserializeString(message));
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}