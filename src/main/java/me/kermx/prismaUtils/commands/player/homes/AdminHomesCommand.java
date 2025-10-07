package me.kermx.prismaUtils.commands.player.homes;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminHomesCommand extends BaseCommand {
    private final PrismaUtils plugin;
    private final HomesCommand homesCommand;

    public AdminHomesCommand(PrismaUtils plugin, HomesCommand homesCommand) {
        super("prismautils.command.adminhome", false, "/adminhome <list|tp|del> <player> [home]");
        this.plugin = plugin;
        this.homesCommand = homesCommand;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtils.deserializeString("<red>This command can only be used by players."));
            return true;
        }

        if (args.length < 2) {
            sendUsage(player);
            return false;
        }

        String action = args[0].toLowerCase();
        String homeCommandAction = "";

        // Map the admin command to the correct home command action
        switch (action) {
            case "list" -> homeCommandAction = "adminlist";
            case "tp" -> homeCommandAction = "tp";
            case "del" -> homeCommandAction = "admindel";
            default -> {
                sendUsage(player);
                return false;
            }
        }

        // Create a new args array to delegate to the HomesCommand
        String[] newArgs = new String[args.length];
        newArgs[0] = homeCommandAction;
        System.arraycopy(args, 1, newArgs, 1, args.length - 1);

        return homesCommand.onCommandExecute(sender, label, newArgs);
    }

    private void sendUsage(Player player) {
        player.sendMessage(TextUtils.deserializeString("<green>Admin Homes Commands:"));
        player.sendMessage(TextUtils.deserializeString("<yellow>/ahome list <player> <white>- List all homes of a player"));
        player.sendMessage(TextUtils.deserializeString("<yellow>/ahome tp <player> <home> <white>- Teleport to a player's home"));
        player.sendMessage(TextUtils.deserializeString("<yellow>/ahome del <player> <home> <white>- Delete a player's home"));
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("list", "tp", "del").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // For all subcommands, suggest players
            return null; // Return null to use Bukkit's default player name completion
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("del"))) {
            // For home names, delegate to the homes command for improved consistency
            String[] newArgs = new String[3];
            if (args[0].equalsIgnoreCase("tp")) {
                newArgs[0] = "tp";
            } else {
                newArgs[0] = "admindel";
            }
            newArgs[1] = args[1];
            newArgs[2] = args[2];

            // Get just the home name suggestions
            return homesCommand.onTabCompleteExecute(sender, newArgs);
        }

        return new ArrayList<>();
    }
}
