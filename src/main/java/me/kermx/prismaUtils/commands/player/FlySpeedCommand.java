package me.kermx.prismaUtils.commands.player;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FlySpeedCommand extends BaseCommand {

    public FlySpeedCommand(){
        super("prismautils.command.flyspeed", false, "/flyspeed");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
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
            if (speed < 0 || speed > 10) {
                sender.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().flyspeedInvalidSpeedMessage)
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

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reset");
            for (int i = 0; i <= 10; i++) {
                completions.add(String.valueOf(i));
            }
        }
        return completions;
    }
}
