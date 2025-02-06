package me.kermx.prismaUtils.Commands.utility;

import me.kermx.prismaUtils.Commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PingCommand extends BaseCommand {

    public PingCommand(){
        super("prismautils.command.ping", true, "/ping");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length == 0){
            if (sender instanceof Player player){
                int ping = player.getPing();

                player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().pingMessage,
                        Placeholder.component("ping", Component.text(ping))));

            } else {
                sender.sendMessage("You must specify a player name from the console!");
            }
        } else if (args.length == 1) {
            if (sender.hasPermission("prismautils.command.ping.others")){
                Player target = Bukkit.getPlayerExact(args[0]);

                if (target != null && target.isOnline()){
                    int ping = target.getPing();

                    sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().pingOtherMessage,
                            Placeholder.component("target", target.displayName()),
                            Placeholder.component("ping", Component.text(ping))));
                } else {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().playerNotFoundMessage));
                }
            } else {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().noPermissionMessage));
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partialArg = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                String name = player.getName().toLowerCase();
                if (name.startsWith(partialArg)) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }
}
