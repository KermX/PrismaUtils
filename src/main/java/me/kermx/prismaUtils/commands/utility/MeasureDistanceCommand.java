package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.GenUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class MeasureDistanceCommand extends BaseCommand {

    private final Map<UUID, Location> firstPositions = new HashMap<>();

    public MeasureDistanceCommand() {
        super("prismautils.command.measure", false, "/measure pos1|pos2 [x] [y] [z]");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length < 1 || args.length > 4) {
            return false;
        }

        Player player = (Player) sender;

        if (args[0].equalsIgnoreCase("pos1")) {
            if (args.length == 4) {
                double x, y, z;
                try {
                    x = Double.parseDouble(args[1]);
                    y = Double.parseDouble(args[2]);
                    z = Double.parseDouble(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(
                            TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().measureInvalidCoordinatesMessage)
                    );
                    return true;
                }
                firstPositions.put(player.getUniqueId(), new Location(player.getWorld(), x, y, z));
            } else {
                firstPositions.put(player.getUniqueId(), player.getLocation());
            }
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().measureFirstPositionSetMessage)
            );
            return true;
        }

        if (args[0].equalsIgnoreCase("pos2")) {
            if (!firstPositions.containsKey(player.getUniqueId())) {
                player.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().measureNoFirstPositionSetMessage)
                );
                return true;
            }
            Location firstPosition = firstPositions.get(player.getUniqueId());
            Location secondPosition;

            if (args.length == 4) {
                double x, y, z;
                try {
                    x = Double.parseDouble(args[1]);
                    y = Double.parseDouble(args[2]);
                    z = Double.parseDouble(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(
                            TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().measureInvalidCoordinatesMessage)
                    );
                    return true;
                }
                secondPosition = new Location(player.getWorld(), x, y, z);
            } else {
                secondPosition = player.getLocation();
            }

            if (!firstPosition.getWorld().equals(secondPosition.getWorld())) {
                player.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().measureDifferentWorldsMessage)
                );
                return true;
            }

            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().measureSecondPositionSetMessage)
            );

            double distance = GenUtils.round(firstPosition.distance(secondPosition), 2);
            double dx = GenUtils.round(Math.abs(secondPosition.getX() - firstPosition.getX()), 2);
            double dy = GenUtils.round(Math.abs(secondPosition.getY() - firstPosition.getY()), 2);
            double dz = GenUtils.round(Math.abs(secondPosition.getZ() - firstPosition.getZ()), 2);
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().measureDistanceMessage,
                            Placeholder.component("distance", Component.text(distance)))
            );
            player.sendMessage(TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().measureDifferenceMessage,
                    Placeholder.component("x", Component.text(dx)),
                    Placeholder.component("y", Component.text(dy)),
                    Placeholder.component("z", Component.text(dz)))
            );
            double midX = GenUtils.round((firstPosition.getX() + secondPosition.getX()) / 2, 0);
            double midY = GenUtils.round((firstPosition.getY() + secondPosition.getY()) / 2, 0);
            double midZ = GenUtils.round((firstPosition.getZ() + secondPosition.getZ()) / 2, 0);
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().measureMidpointMessage,
                            Placeholder.component("x", Component.text(midX)),
                            Placeholder.component("y", Component.text(midY)),
                            Placeholder.component("z", Component.text(midZ)))
            );

            firstPositions.remove(player.getUniqueId());
            return true;
        }
        return false;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("pos1", "pos2");
        } else {
            if (args[0].equalsIgnoreCase("pos1") || args[0].equalsIgnoreCase("pos2")) {
                Location location = ((Player) sender).getLocation();
                int x = (int) location.getX();
                int y = (int) location.getY();
                int z = (int) location.getZ();

                if (args.length == 2) {
                    return Collections.singletonList(String.valueOf(x));
                }
                if (args.length == 3) {
                    return Collections.singletonList(String.valueOf(y));
                }
                if (args.length == 4) {
                    return Collections.singletonList(String.valueOf(z));
                }
            }
        }
        return Collections.emptyList();
    }
}