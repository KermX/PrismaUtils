package me.kermx.prismaUtils.commands.player.homes;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetHomeCommand extends BaseCommand {
    private final PrismaUtils plugin;
    private final HomesCommand homesCommand;

    public SetHomeCommand(PrismaUtils plugin, HomesCommand homesCommand) {
        super("prismautils.command.sethome", false, "/sethome [name]");
        this.plugin = plugin;
        this.homesCommand = homesCommand;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtils.deserializeString("<red>This command can only be used by players."));
            return true;
        }

        // Create a new args array with "set" as the first argument
        String[] newArgs;
        if (args.length > 0) {
            newArgs = new String[args.length + 1];
            newArgs[0] = "set";
            System.arraycopy(args, 0, newArgs, 1, args.length);
        } else {
            newArgs = new String[]{"set"};
        }

        return homesCommand.onCommandExecute(sender, label, newArgs);
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}

