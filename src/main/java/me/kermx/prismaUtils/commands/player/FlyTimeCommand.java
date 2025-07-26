package me.kermx.prismaUtils.commands.player;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.features.FlightManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class FlyTimeCommand extends BaseCommand {

    private final FlightManager flightManager;

    public FlyTimeCommand(FlightManager flightManager) {
        super("prismautils.command.flytime", false, "/flytime");
        this.flightManager = flightManager;
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        long remainingTime = flightManager.getRemainingTempFlightTime(player);

        if (remainingTime > 0) {
            // Format time into a more readable format
            String formattedTime = formatTime(remainingTime);
            player.sendMessage(TextUtils.deserializeString(
                    "<green>You have <white><time></white> of temporary flight remaining.</green>",
                    Placeholder.unparsed("time", formattedTime)
            ));
        } else {
            player.sendMessage(TextUtils.deserializeString(
                    "<yellow>You have no temporary flight time remaining.</yellow>"
            ));
        }

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        // No tab completion needed for this command
        return List.of();
    }

    /**
     * Formats seconds into a human-readable time format
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            if (remainingSeconds == 0) {
                return minutes + " minutes";
            } else {
                return minutes + " minutes " + remainingSeconds + " seconds";
            }
        } else {
            long hours = seconds / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            long remainingSeconds = seconds % 60;

            StringBuilder timeString = new StringBuilder();
            timeString.append(hours).append(" hours");

            if (remainingMinutes > 0) {
                timeString.append(" ").append(remainingMinutes).append(" minutes");
            }
            if (remainingSeconds > 0) {
                timeString.append(" ").append(remainingSeconds).append(" seconds");
            }

            return timeString.toString();
        }
    }
}

