package me.kermx.prismaUtils.commands.player.warps;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.playerdata.PlayerData;
import me.kermx.prismaUtils.managers.core.CooldownManager;
import me.kermx.prismaUtils.managers.config.WarpsConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WarpCommand extends BaseCommand {
    private final WarpsConfigManager warpsConfigManager;
    private final PrismaUtils plugin;

    public WarpCommand(WarpsConfigManager warpsConfigManager, PrismaUtils plugin) {
        super("prismautils.command.warp", false, "/warp <name>");
        this.warpsConfigManager = warpsConfigManager;
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            // If no arguments, list available warps with clickable elements
            listClickableWarps(player);
            return true;
        }

        String warpName = args[0];
        return teleportToWarp(player, warpName);
    }

    private boolean teleportToWarp(Player player, String warpName) {
        WarpsConfigManager.WarpData warpData = warpsConfigManager.getWarpData(warpName);

        if (warpData == null) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>Warp [<white>" + warpName + "<red>] doesn't exist."
            ));
            return true;
        }

        String warpPermission = warpData.getPermission();
        if (warpPermission != null && !player.hasPermission(warpPermission)) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>You don't have permission to use warp [<white>" + warpName + "<red>]."
            ));
            return true;
        }

        CooldownManager cooldownManager = CooldownManager.getInstance();
        if (!cooldownManager.canUseWarpTeleport(player)) {
            int remainingSeconds = cooldownManager.getWarpCooldownRemaining(player);
            player.sendMessage(TextUtils.deserializeString("<red>You must wait <white>" + remainingSeconds + " second" + (remainingSeconds == 1 ? "" : "s") + "<red> before using this command again."));
            return true;
        }

        // Save the current location for /back command
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (playerData != null) {
            playerData.setLastLocation(player.getLocation().clone());
        }

        // Apply cooldown
        cooldownManager.setWarpTeleportCooldown(player);

        // Teleport player to warp
        player.teleportAsync(warpData.getLocation());
        player.sendMessage(TextUtils.deserializeString(
                "<green>Teleported to warp [<white>" + warpName + "<green>]."
        ));

        return true;
    }

    private void listClickableWarps(Player player) {
        Set<String> warpNames = warpsConfigManager.getWarpNames();

        if (warpNames.isEmpty()) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>There are no warps set up yet."
            ));
            return;
        }

        player.sendMessage(TextUtils.deserializeString(
                "<green>Available warps:"
        ));

        // Create a list of all warp components
        List<Component> warpComponents = new ArrayList<>();

        // Create a clickable component for each warp
        for (String warpName : warpNames) {
            WarpsConfigManager.WarpData warpData = warpsConfigManager.getWarpData(warpName);

            if (warpData != null) {
                // Check if player has permission for this warp (only show if they can use it)
                String warpPermission = warpData.getPermission();
                if (warpPermission != null && !player.hasPermission(warpPermission)) {
                    continue; // Skip this warp if player doesn't have permission
                }

                Location loc = warpData.getLocation();
                // Format coordinates to be more readable
                String coords = String.format("%.0f, %.0f, %.0f", loc.getX(), loc.getY(), loc.getZ());

                // Create hover text with world and coordinates
                String hoverText = String.format("<green>World: <white>%s\n<green>Coordinates: <white>%s\n<yellow>Click to teleport!",
                        loc.getWorld().getName(), coords);

                // Create the clickable warp component
                Component warpComponent = TextUtils.deserializeString("<green>[<white>" + warpName + "<green>]")
                        .clickEvent(ClickEvent.runCommand("/warp " + warpName))
                        .hoverEvent(HoverEvent.showText(TextUtils.deserializeString(hoverText)));

                warpComponents.add(warpComponent);
            }
        }

        if (warpComponents.isEmpty()) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>You don't have permission to use any warps."
            ));
            return;
        }

        // Display warps in groups of 5 per line
        int warpsPerLine = 5;
        for (int i = 0; i < warpComponents.size(); i += warpsPerLine) {
            Component lineComponent = Component.empty();

            // Add warps to this line
            for (int j = i; j < Math.min(i + warpsPerLine, warpComponents.size()); j++) {
                if (j > i) {
                    // Add space between warps
                    lineComponent = lineComponent.append(TextUtils.deserializeString(" "));
                }
                lineComponent = lineComponent.append(warpComponents.get(j));
            }

            // Send this line to the player
            player.sendMessage(lineComponent);
        }
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            Player player = (Player) sender;
            String partial = args[0].toLowerCase();
            return warpsConfigManager.getWarpNames().stream()
                    .filter(warp -> warp.toLowerCase().startsWith(partial))
                    .filter(warp -> {
                        // Only show warps the player has permission to use
                        WarpsConfigManager.WarpData warpData = warpsConfigManager.getWarpData(warp);
                        if (warpData == null) return false;
                        String permission = warpData.getPermission();
                        return permission == null || player.hasPermission(permission);
                    })
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}

