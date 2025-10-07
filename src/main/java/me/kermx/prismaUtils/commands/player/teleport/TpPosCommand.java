package me.kermx.prismaUtils.commands.player.teleport;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.playerdata.PlayerData;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TpPosCommand extends BaseCommand {

    private final PrismaUtils plugin;

    public TpPosCommand(PrismaUtils plugin) {
        super("prismautils.command.tppos", false, "/tppos <x> <y> <z> [world]");
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length < 3) {
            return false;
        }

        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);

            World world;
            if (args.length >= 4) {
                world = Bukkit.getWorld(args[3]);
                if (world == null) {
                    player.sendMessage(TextUtils.deserializeString(
                            "<red>World '<white>" + args[3] + "<red>' not found."
                    ));
                    return true;
                }
            } else {
                world = player.getWorld();
            }

            // Store the player's last location for /back command
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (playerData != null) {
                playerData.setLastLocation(player.getLocation().clone());
            }

            // Create the destination location
            Location destination = new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());

            // Teleport the player
            player.teleportAsync(destination);
            player.sendMessage(TextUtils.deserializeString(
                    "<green>Teleported to <white>" + String.format("%.1f", x) + ", " +
                            String.format("%.1f", y) + ", " + String.format("%.1f", z) +
                            "<green> in world <white>" + world.getName() + "<green>."
            ));

            return true;
        } catch (NumberFormatException e) {
            player.sendMessage(TextUtils.deserializeString(
                    "<red>Invalid coordinates. Use numbers for x, y, and z."
            ));
            return true;
        }
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        if (args.length <= 3) {
            Location loc = player.getLocation();
            String suggestion = "";

            switch (args.length) {
                case 1 -> suggestion = String.valueOf(Math.round(loc.getX()));
                case 2 -> suggestion = String.valueOf(Math.round(loc.getY()));
                case 3 -> suggestion = String.valueOf(Math.round(loc.getZ()));
            }

            List<String> result = new ArrayList<>();
            if (!suggestion.isEmpty()) {
                result.add(suggestion);
            }
            return result;
        } else if (args.length == 4) {
            String partial = args[3].toLowerCase();
            return Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
