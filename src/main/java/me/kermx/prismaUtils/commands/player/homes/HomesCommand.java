package me.kermx.prismaUtils.commands.player.homes;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.PlayerData.Home;
import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//TODO: Add a way for admins to teleport to player homes
//TODO: Add a way for admins to delete player homes
//TODO: Add a way for admins to list player homes
//TODO: Make it so /home list is clickable to teleport to a home

public class HomesCommand extends BaseCommand {

    private final PrismaUtils plugin;
    private static final String DEFAULT_HOME_NAME = "home";
    private static final String HOME_LIMIT_PERMISSION_PREFIX = "prismautils.homes.limit.";
    private static final String ADMIN_PERMISSION = "prismautils.admin.homes";

    public HomesCommand(PrismaUtils plugin) {
        super("prismautils.command.homes", false, "/home [set|del|list|help] [name]");
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        String subCommand = args.length > 0 ? args[0].toLowerCase() : "";

        switch (subCommand) {
            case "set", "sethome", "create" -> {
                return handleSetHome(player, args);
            }
            case "del", "delete", "remove" -> {
                return handleDeleteHome(player, args);
            }
            case "list" -> {
                return handleListHomes(player);
            }
            case "help" -> {
                sendHelp(player);
                return true;
            }
            default -> {
                if (subCommand.isEmpty() && args.length == 0) {
                    return handleTeleport(player, DEFAULT_HOME_NAME);
                } else {
                    return handleTeleport(player, subCommand);
                }
            }
        }
    }

    private boolean handleSetHome (Player player, String[] args) {
        if (!player.hasPermission("prismautils.command.sethome")) {
            player.sendMessage(TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().noPermissionMessage));
            return true;
        }

        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        String homeName = args.length > 1 ? args[1] : DEFAULT_HOME_NAME;

        int currentHomes = playerData.getHomesCount();
        int homeLimit = getHomeLimit(player);

        boolean homeExists = playerData.getHome(homeName) != null;
        if (!homeExists && currentHomes >= homeLimit) {
            player.sendMessage(TextUtils.deserializeString("<red>You have reached your home limit of " + homeLimit + "."));
            return true;
        }

        Home home = new Home(homeName, player.getLocation());
        playerData.addHome(homeName, home);

        player.sendMessage(TextUtils.deserializeString("<green>Home '" + homeName + "' has been set."));
        return true;
    }

    private boolean handleDeleteHome(Player player, String[] args) {
        if (!player.hasPermission("prismautils.command.delhome")) {
            player.sendMessage(TextUtils.deserializeString("<red>You don't have permission to delete homes."));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(TextUtils.deserializeString("<red>Usage: /home del <name>"));
            return false;
        }

        String homeName = args[1];
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        boolean removed = playerData.removeHome(homeName);
        if (removed) {
            player.sendMessage(TextUtils.deserializeString("<green>Home '" + homeName + "' has been deleted."));
        } else {
            player.sendMessage(TextUtils.deserializeString("<red>You don't have a home named '" + homeName + "'."));
        }
        return true;
    }

    private boolean handleListHomes(Player player) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        Map<String, Home> homes = playerData.getHomes();

        if (homes.isEmpty()) {
            player.sendMessage(TextUtils.deserializeString("<yellow>You don't have any homes set."));
            return true;
        }

        int homeLimit = getHomeLimit(player);
        player.sendMessage(TextUtils.deserializeString("<green>Your homes (" + homes.size() + "/" + homeLimit + "):"));

        for (Home home : homes.values()) {
            Location loc = home.getLocation();
            if (loc != null) {
                player.sendMessage(TextUtils.deserializeString(
                        "<yellow>" + home.getName() + ": <white>" +
                                loc.getWorld().getName() + " (" +
                                Math.round(loc.getX()) + ", " +
                                Math.round(loc.getY()) + ", " +
                                Math.round(loc.getZ()) + ")"
                ));
            }
        }
        return true;
    }

    private boolean handleTeleport(Player player, String homeName) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        Home home = playerData.getHome(homeName);

        if (home == null) {
            player.sendMessage(TextUtils.deserializeString("<red>You don't have a home named '" + homeName + "'."));
            return true;
        }

        Location location = home.getLocation();
        if (location == null) {
            player.sendMessage(TextUtils.deserializeString("<red>The world of this home doesn't exist anymore."));
            return true;
        }

        player.teleport(location);
        player.sendMessage(TextUtils.deserializeString("<green>Teleported to home '" + homeName + "'."));
        return true;
    }

    private int getHomeLimit(Player player) {
        if (player.hasPermission(ADMIN_PERMISSION)) {
            return Integer.MAX_VALUE; // Unlimited homes for admins
        }

        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission(HOME_LIMIT_PERMISSION_PREFIX + i)) {
                return i;
            }
        }

        return 1;
    }

    private void sendHelp(Player player) {
        player.sendMessage(TextUtils.deserializeString("<green>--- Homes Help ---"));
        player.sendMessage(TextUtils.deserializeString("<yellow>/home <name> <white>- Teleport to a home"));
        player.sendMessage(TextUtils.deserializeString("<yellow>/home set <name> <white>- Set a home at your current location"));
        player.sendMessage(TextUtils.deserializeString("<yellow>/home del <name> <white>- Delete a home"));
        player.sendMessage(TextUtils.deserializeString("<yellow>/home list <white>- List all your homes"));
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("set", "del", "list", "help");
            String lowercaseArg = args[0].toLowerCase();

            // Add home names for teleporting
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            List<String> allOptions = new ArrayList<>(subCommands);
            allOptions.addAll(playerData.getHomes().keySet());

            return allOptions.stream()
                    .filter(s -> s.toLowerCase().startsWith(lowercaseArg))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // For additional actions, suggest home names
            if (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete")) {
                PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                return playerData.getHomes().keySet().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}
