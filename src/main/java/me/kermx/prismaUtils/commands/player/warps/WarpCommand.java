package me.kermx.prismaUtils.commands.player.warps;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.PlayerData.PlayerData;
import me.kermx.prismaUtils.managers.general.configs.WarpsConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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
        Location warpLocation = warpsConfigManager.getWarp(warpName);

        if (warpLocation == null) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>Warp [<white>" + warpName + "<red>] doesn't exist."
            ));
            return true;
        }

        // Save the current location for /back command
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (playerData != null) {
            playerData.setLastLocation(player.getLocation().clone());
        }

        // Teleport player to warp
        player.teleportAsync(warpLocation);
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
                "<green>Available warps: <gray>(click to teleport)"
        ));

        // Instead of just joining the names, create a component for each warp
        Component message = Component.empty();
        boolean first = true;

        for (String warpName : warpNames) {
            if (!first) {
                message = message.append(Component.text(", ").color(NamedTextColor.GRAY));
            }

            // Create a clickable component for each warp
            Component warpComponent = Component.text(warpName)
                    .color(NamedTextColor.WHITE)
                    .clickEvent(ClickEvent.runCommand("/warp " + warpName))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to teleport to " + warpName)
                            .color(NamedTextColor.GREEN)));

            message = message.append(warpComponent);
            first = false;
        }

        player.sendMessage(message);
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return warpsConfigManager.getWarpNames().stream()
                    .filter(warp -> warp.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
