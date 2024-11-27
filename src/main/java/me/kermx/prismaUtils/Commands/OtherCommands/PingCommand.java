package me.kermx.prismaUtils.Commands.OtherCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("prismautils.command.ping")){
            sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            return true;
        }

        if (args.length == 0){
            if (sender instanceof Player player){
                int ping = player.getPing();

                player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().pingMessage,
                        Placeholder.component("ping", Component.text(ping))));

            } else {
                sender.sendMessage("Only players can use this command!");
            }
        } else if (args.length == 1) {
            if (sender.hasPermission("prismautils.command.ping.others")){
                Player target = Bukkit.getPlayer(args[0]);

                if (target != null && target.isOnline()) {
                    int ping = target.getPing();

                    sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().pingOtherMessage,
                            Placeholder.component("target", target.displayName()),
                            Placeholder.component("ping", Component.text(ping))));
                } else {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().playerNotFoundMessage));
                }
            } else {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            }
        } else {
            sender.sendMessage("Usage: /ping [player]");
        }

        return true;
    }
}
