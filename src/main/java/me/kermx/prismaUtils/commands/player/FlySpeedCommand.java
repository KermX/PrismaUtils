package me.kermx.prismaUtils.commands.player;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FlySpeedCommand extends BaseCommand {

    private static final int DEFAULT_MAX_SPEED = 10;
    private static final String MAX_SPEED_PERMISSION_PREFIX = "prismautils.command.flyspeed.max.";

    public FlySpeedCommand() {
        super("prismautils.command.flyspeed", false, "/flyspeed");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("reset")) {
            if (sender instanceof Player player) {
                player.setFlySpeed(0.1f);
                player.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().flyspeedResetMessage)
                );
                return true;
            }
        }
        try {
            float speed = Float.parseFloat(args[0]);
            int maxAllowedSpeed = getMaxAllowedSpeed((Player) sender);
            if (speed < 0 || speed > maxAllowedSpeed) {
                Component maxSpeedComponent = Component.text(maxAllowedSpeed);
                sender.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().flyspeedInvalidSpeedMessage,
                        Placeholder.component("maxspeed", maxSpeedComponent))
                );
                return true;
            }
            float adjustedSpeed = speed / 10.0f;
            ((Player) sender).setFlySpeed(adjustedSpeed);

            Component speedComponent = Component.text(speed);
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().flyspeedSetMessage,
                    Placeholder.component("speed", speedComponent))
            );

        } catch (NumberFormatException e) {
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().flyspeedInvalidSpeedMessage)
            );
        }
        return true;
    }

    private int getMaxAllowedSpeed(Player player) {
        int maxSpeed = DEFAULT_MAX_SPEED;
        for (int i = 0; i <= DEFAULT_MAX_SPEED; i++) {
            if (player.hasPermission(MAX_SPEED_PERMISSION_PREFIX + i)) {
                maxSpeed = i;
            }
        }
        return maxSpeed;
    }


    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reset");
            int maxSpeed = DEFAULT_MAX_SPEED;
            if (sender instanceof Player player) {
                maxSpeed = getMaxAllowedSpeed(player);
            }
            for (int i = 0; i <= maxSpeed; i++) {
                completions.add(String.valueOf(i));
            }
        }
        return completions;
    }
}