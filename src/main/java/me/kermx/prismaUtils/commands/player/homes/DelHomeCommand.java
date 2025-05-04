package me.kermx.prismaUtils.commands.player.homes;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class DelHomeCommand extends BaseCommand {
    private final PrismaUtils plugin;
    private final HomesCommand homesCommand;

    public DelHomeCommand(PrismaUtils plugin, HomesCommand homesCommand) {
        super("prismautils.command.delhome", false, "/delhome <name>");
        this.plugin = plugin;
        this.homesCommand = homesCommand;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtils.deserializeString("<red>This command can only be used by players."));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(TextUtils.deserializeString("<red>Please specify a home name."));
            return false;
        }

        // Create a new args array with "del" as the first argument
        String[] newArgs = new String[args.length + 1];
        newArgs[0] = "del";
        System.arraycopy(args, 0, newArgs, 1, args.length);

        return homesCommand.onCommandExecute(sender, label, newArgs);
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        if (args.length == 1) {
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            String lowercaseArg = args[0].toLowerCase();

            return playerData.getHomes().keySet().stream()
                    .filter(s -> s.toLowerCase().startsWith(lowercaseArg))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}

