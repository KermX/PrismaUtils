package me.kermx.prismaUtils.commands.player.homes;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.playerdata.Home;
import me.kermx.prismaUtils.managers.playerdata.PlayerData;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.managers.core.CooldownManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class HomesCommand extends BaseCommand {
    private final PrismaUtils plugin;
    private static final String DEFAULT_HOME_NAME = "home";
    private static final String ADMIN_PERMISSION = "prismautils.admin.homes";
    private static final String HOME_LIMIT_PERMISSION_PREFIX = "prismautils.homes.limit.";

    public HomesCommand(PrismaUtils plugin) {
        super("prismautils.command.home", false, "/home [set|del|list|help] [name]");
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtils.deserializeString("<red>This command can only be used by players."));
            return true;
        }

        String subCommand = args.length > 0 ? args[0].toLowerCase() : "";

        switch (subCommand) {
            case "set", "sethome" -> {
                return handleSetHome(player, args);
            }
            case "del", "delete", "delhome" -> {
                return handleDeleteHome(player, args);
            }
            case "list" -> {
                return handleListHomes(player, args);
            }
            case "help" -> {
                sendHelp(player);
                return true;
            }
            // Admin commands
            case "tp", "teleport" -> {
                return handleAdminTeleport(player, args);
            }
            case "admindel", "adelete" -> {
                return handleAdminDeleteHome(player, args);
            }
            case "alist", "adminlist" -> {
                return handleAdminListHomes(player, args);
            }
            default -> {
                if (subCommand.isEmpty() && args.length == 0) {
                    // Check if command is "homes" or if there's no home named "home"
                    if (label.equalsIgnoreCase("homes")) {
                        return handleListHomes(player, args);
                    }

                    // Get player data to check if "home" exists
                    PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                    Home defaultHome = playerData.getHome(DEFAULT_HOME_NAME);

                    // If there's no default home, show the home list instead
                    if (defaultHome == null) {
                        return handleListHomes(player, args);
                    }

                    // Otherwise, teleport to the default home
                    return handleTeleport(player, DEFAULT_HOME_NAME);
                } else {
                    // Treat the first arg as home name for teleportation
                    return handleTeleport(player, subCommand);
                }
            }
        }
    }


    private boolean handleSetHome(Player player, String[] args) {
        if (!player.hasPermission("prismautils.command.sethome")) {
            player.sendMessage(TextUtils.deserializeString("<red>You don't have permission to set homes."));
            return true;
        }

        // Get player data
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // Default home name is "home" if not specified
        String homeName = args.length > 1 ? args[1] : DEFAULT_HOME_NAME;

        if (ConfigManager.getInstance().getMainConfig().homeNameBlacklist.contains(homeName)) {
            player.sendMessage(TextUtils.deserializeString("<red>You cannot use that home name."));
            return true;
        }

        int maxHomeNameLength = 16;
        if (homeName.length() > maxHomeNameLength) {
            player.sendMessage(TextUtils.deserializeString("<red>Home names must be less than " + maxHomeNameLength + "<red>characters."));
            return true;
        }

        // Check if player has reached their home limit
        int currentHomes = playerData.getHomesCount();
        int homeLimit = getHomeLimit(player);

        // Check if setting this home would exceed limit
        boolean homeExists = playerData.getHome(homeName) != null;
        if (!homeExists && currentHomes >= homeLimit) {
            player.sendMessage(TextUtils.deserializeString("<red>You have reached your home limit of " + homeLimit + "."));
            return true;
        }

        if (homeExists) {
            // If additional argument for confirmation is not provided
            if (args.length <= 2 || !args[2].equalsIgnoreCase("confirm")) {
                Component confirmMessage = TextUtils.deserializeString(
                                "<yellow>A home named [<white>" + homeName + "<yellow>] already exists. " +
                                        "Click here to overwrite it.")
                        .clickEvent(ClickEvent.runCommand("/home set " + homeName + " confirm"))
                        .hoverEvent(HoverEvent.showText(TextUtils.deserializeString("<green>Click to confirm")));

                player.sendMessage(confirmMessage);
                return true;
            }
            // If confirmation is provided, continue with setting the home
        }

        // Create the home
        Home home = new Home(homeName, player.getLocation());
        playerData.addHome(homeName, home);

        player.sendMessage(TextUtils.deserializeString("<green>Home [<white>" + homeName + "<green>] has been set."));
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

        // Check if the home exists first
        Home home = playerData.getHome(homeName);
        if (home == null) {
            player.sendMessage(TextUtils.deserializeString("<red>You don't have a home named [<white>" + homeName + "<red>]."));
            return true;
        }

        // If confirmation is not provided as third argument
        if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
            // Create a clickable confirmation message in the same style as home overwriting
            Component confirmMessage = TextUtils.deserializeString(
                            "<yellow>Are you sure you want to delete home [<white>" + homeName + "<yellow>]? " +
                                    "Click here to confirm.")
                    .clickEvent(ClickEvent.runCommand("/home del " + homeName + " confirm"))
                    .hoverEvent(HoverEvent.showText(TextUtils.deserializeString("<green>Click to confirm")));

            player.sendMessage(confirmMessage);
            return true;
        }


        // If we get here, the player has confirmed the deletion
        boolean removed = playerData.removeHome(homeName);
        player.sendMessage(TextUtils.deserializeString("<green>Home [<white>" + homeName + "<green>] has been deleted."));
        return true;
    }

    private boolean handleListHomes(Player player, String[] args) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        Map<String, Home> homes = playerData.getHomes();

        if (homes.isEmpty()) {
            player.sendMessage(TextUtils.deserializeString("<yellow>You don't have any homes set."));
            return true;
        }

        int homeLimit = getHomeLimit(player);
        player.sendMessage(TextUtils.deserializeString("<green>Your homes (" + homes.size() + "/" + homeLimit + "):"));

        // Create a list of all home components
        List<Component> homeComponents = new ArrayList<>();

        // Create a clickable component for each home
        for (Home home : homes.values()) {
            Location loc = home.getLocation();
            if (loc != null) {
                // Format coordinates to be more readable
                String coords = String.format("%.0f, %.0f, %.0f", loc.getX(), loc.getY(), loc.getZ());

                // Create hover text with world and coordinates
                String hoverText = String.format("<green>World: <white>%s\n<green>Coordinates: <white>%s\n<yellow>Click to teleport!",
                        loc.getWorld().getName(), coords);

                // Create the clickable home component
                Component homeComponent = TextUtils.deserializeString("<green>[<white>" + home.getName() + "<green>]")
                        .clickEvent(ClickEvent.runCommand("/home " + home.getName()))
                        .hoverEvent(HoverEvent.showText(TextUtils.deserializeString(hoverText)));

                homeComponents.add(homeComponent);
            }
        }

        // Display homes in groups of 5 per line
        int homesPerLine = 5;
        for (int i = 0; i < homeComponents.size(); i += homesPerLine) {
            Component lineComponent = Component.empty();

            // Add homes to this line
            for (int j = i; j < Math.min(i + homesPerLine, homeComponents.size()); j++) {
                if (j > i) {
                    // Add space between homes
                    lineComponent = lineComponent.append(TextUtils.deserializeString(" "));
                }
                lineComponent = lineComponent.append(homeComponents.get(j));
            }

            // Send this line to the player
            player.sendMessage(lineComponent);
        }

        return true;
    }

    private boolean handleTeleport(Player player, String homeName) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        Home home = playerData.getHome(homeName);

        if (home == null) {
            player.sendMessage(TextUtils.deserializeString("<red>You don't have a home named [<white>" + homeName + "<red>] ."));
            return true;
        }

        Location location = home.getLocation();
        if (location == null) {
            player.sendMessage(TextUtils.deserializeString("<red>The world of this home doesn't exist anymore."));
            return true;
        }

        CooldownManager cooldownManager = CooldownManager.getInstance();
        if (!cooldownManager.canUseHomeTeleport(player)) {
            int remainingSeconds = cooldownManager.getHomeCooldownRemaining(player);
            player.sendMessage(TextUtils.deserializeString("<red>You must wait <white>" + remainingSeconds + " second" + (remainingSeconds == 1 ? "" : "s") + "<red> before using this command again."));
            return true;
        }

        playerData.setLastLocation(player.getLocation().clone());

        cooldownManager.setHomeTeleportCooldown(player);

        player.teleportAsync(location);
        player.sendMessage(TextUtils.deserializeString("<green>Teleported to home [<white>" + homeName + "<green>] ."));
        return true;
    }

    /**
     * Handle admin teleport to another player's home
     * Usage: /home tp <player> <home>
     */
    private boolean handleAdminTeleport(Player player, String[] args) {
        if (!player.hasPermission(ADMIN_PERMISSION)) {
            player.sendMessage(TextUtils.deserializeString("<red>You don't have permission to teleport to other players' homes."));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(TextUtils.deserializeString("<red>Usage: /home tp <player> <home>"));
            return false;
        }

        String targetName = args[1];
        String homeName = args[2];

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayerIfCached(targetName);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            player.sendMessage(TextUtils.deserializeString("<red>Player not found or has never played before."));
            return true;
        }

        UUID targetUuid = targetPlayer.getUniqueId();
        PlayerData targetData = plugin.getPlayerDataManager().getPlayerData(targetUuid);

        Home home = targetData.getHome(homeName);
        if (home == null) {
            player.sendMessage(TextUtils.deserializeString("<red>Player '" + targetName + "' doesn't have a home named '" + homeName + "'."));
            return true;
        }

        Location location = home.getLocation();
        if (location == null) {
            player.sendMessage(TextUtils.deserializeString("<red>The world of this home doesn't exist anymore."));
            return true;
        }

        player.teleportAsync(location);
        player.sendMessage(TextUtils.deserializeString(
                "<green>Teleported to " + targetName + "'s home '" + homeName + "'."
        ));
        return true;
    }

    /**
     * Handle admin deletion of another player's home
     * Usage: /home admindel <player> <home>
     */
    private boolean handleAdminDeleteHome(Player player, String[] args) {
        if (!player.hasPermission(ADMIN_PERMISSION)) {
            player.sendMessage(TextUtils.deserializeString("<red>You don't have permission to delete other players' homes."));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(TextUtils.deserializeString("<red>Usage: /home admindel <player> <home>"));
            return false;
        }

        String targetName = args[1];
        String homeName = args[2];

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayerIfCached(targetName);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            player.sendMessage(TextUtils.deserializeString("<red>Player not found or has never played before."));
            return true;
        }

        UUID targetUuid = targetPlayer.getUniqueId();
        PlayerData targetData = plugin.getPlayerDataManager().getPlayerData(targetUuid);

        boolean removed = targetData.removeHome(homeName);
        if (removed) {
            player.sendMessage(TextUtils.deserializeString(
                    "<green>Deleted " + targetName + "'s home '" + homeName + "'."
            ));

            // Save the data immediately since this is an admin action
            plugin.getPlayerDataManager().savePlayerData(targetUuid);
        } else {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>Player '" + targetName + "' doesn't have a home named '" + homeName + "'."
            ));
        }
        return true;
    }

    /**
     * Handle admin listing of another player's homes
     * Usage: /home adminlist <player>
     */
    private boolean handleAdminListHomes(Player player, String[] args) {
        if (!player.hasPermission(ADMIN_PERMISSION)) {
            player.sendMessage(TextUtils.deserializeString("<red>You don't have permission to list other players' homes."));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(TextUtils.deserializeString("<red>Usage: /home adminlist <player>"));
            return false;
        }

        String targetName = args[1];

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayerIfCached(targetName);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            player.sendMessage(TextUtils.deserializeString("<red>Player not found or has never played before."));
            return true;
        }

        UUID targetUuid = targetPlayer.getUniqueId();
        PlayerData targetData = plugin.getPlayerDataManager().getPlayerData(targetUuid);
        Map<String, Home> homes = targetData.getHomes();

        if (homes.isEmpty()) {
            player.sendMessage(TextUtils.deserializeString("<yellow>Player '" + targetName + "' doesn't have any homes."));
            return true;
        }

        player.sendMessage(TextUtils.deserializeString("<green>" + targetName + "'s homes (" + homes.size() + "):"));

        // Send clickable home list with teleport and delete options
        for (Home home : homes.values()) {
            Location loc = home.getLocation();
            if (loc != null) {
                String message = "<yellow>" + home.getName() + ": <white>" +
                        loc.getWorld().getName() + " (" +
                        Math.round(loc.getX()) + ", " +
                        Math.round(loc.getY()) + ", " +
                        Math.round(loc.getZ()) + ")";

                Component component = TextUtils.deserializeString(message)
                        .clickEvent(ClickEvent.runCommand("/home tp " + targetName + " " + home.getName()))
                        .hoverEvent(HoverEvent.showText(TextUtils.deserializeString(
                                "<green>Click to teleport to this home\n" +
                                        "<red>Use /home admindel " + targetName + " " + home.getName() + " to delete"
                        )));

                player.sendMessage(component);
            }
        }
        return true;
    }

    private int getHomeLimit(Player player) {
        if (player.hasPermission(ADMIN_PERMISSION)) {
            return Integer.MAX_VALUE; // Unlimited homes for admins
        }

        // Check for numbered limits from highest to lowest
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission(HOME_LIMIT_PERMISSION_PREFIX + i)) {
                return i;
            }
        }

        // Default limit is 1 if no specific permission
        return 1;
    }

    private void sendHelp(Player player) {
        player.sendMessage(TextUtils.deserializeString("<green>--- Homes Help ---"));
        player.sendMessage(TextUtils.deserializeString("<yellow>/home <name> <white>- Teleport to a home"));
        player.sendMessage(TextUtils.deserializeString("<yellow>/home set <name> <white>- Set a home at your current location"));
        player.sendMessage(TextUtils.deserializeString("<yellow>/home del <name> <white>- Delete a home"));
        player.sendMessage(TextUtils.deserializeString("<yellow>/home list <white>- List all your homes"));

        // Show admin commands if player has permission
        if (player.hasPermission(ADMIN_PERMISSION)) {
            player.sendMessage(TextUtils.deserializeString("<green>--- Admin Commands ---"));
            player.sendMessage(TextUtils.deserializeString("<yellow>/home tp <player> <home> <white>- Teleport to a player's home"));
            player.sendMessage(TextUtils.deserializeString("<yellow>/home admindel <player> <home> <white>- Delete a player's home"));
            player.sendMessage(TextUtils.deserializeString("<yellow>/home adminlist <player> <white>- List a player's homes"));
        }
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("set", "del", "list", "help");
            List<String> allOptions = new ArrayList<>(subCommands);

            // Add admin commands if player has permission
            if (player.hasPermission(ADMIN_PERMISSION)) {
                allOptions.addAll(Arrays.asList("tp", "admindel", "adminlist"));
            }

            // Add home names for teleporting
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            allOptions.addAll(playerData.getHomes().keySet());

            return filterStartsWith(allOptions, args[0]);
        } else if (args.length == 2) {
            // For delete command, suggest home names
            if (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete")) {
                PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                return filterStartsWith(new ArrayList<>(playerData.getHomes().keySet()), args[1]);
            }

            // For admin commands, suggest players
            if ((args[0].equalsIgnoreCase("tp") ||
                    args[0].equalsIgnoreCase("admindel") ||
                    args[0].equalsIgnoreCase("adminlist")) &&
                    player.hasPermission(ADMIN_PERMISSION)) {
                return null; // Return null to use Bukkit's default player name completion
            }
        } else if (args.length == 3) {
            // For tp and admindel commands, suggest the target player's home names
            if ((args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("admindel")) &&
                    player.hasPermission(ADMIN_PERMISSION)) {

                OfflinePlayer targetPlayer = Bukkit.getOfflinePlayerIfCached(args[1]);
                if (targetPlayer != null && targetPlayer.hasPlayedBefore()) {
                    PlayerData targetData = plugin.getPlayerDataManager().getPlayerData(targetPlayer.getUniqueId());
                    return filterStartsWith(new ArrayList<>(targetData.getHomes().keySet()), args[2]);
                }
            }
        }

        return new ArrayList<>();
    }

    private List<String> filterStartsWith(List<String> options, String prefix) {
        String lowercasePrefix = prefix.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lowercasePrefix))
                .collect(Collectors.toList());
    }
}
