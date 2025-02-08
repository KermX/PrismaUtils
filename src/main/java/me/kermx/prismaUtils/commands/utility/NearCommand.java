package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class NearCommand extends BaseCommand {

    public NearCommand(){
        super("prismautils.command.near", false, "/near [radius]");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        Location location = ((Player) sender).getLocation();
        double radius = 100;
        if (args.length > 0) {
            try {
                radius = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().nearInvalidRadiusMessage));
                return false;
            }
        }
        Component radiusComponent = Component.text(radius);
        sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().nearNearPlayersMessage,
                Placeholder.component("radius", radiusComponent)));
        boolean found = false;

        for( Player onlinePlayer : ((Player) sender).getWorld().getPlayers() ){
            if( onlinePlayer.getLocation().distance(location) <= radius && onlinePlayer != sender ){
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().nearNearbyPlayersMessage,
                        Placeholder.component("player", onlinePlayer.displayName()),
                        Placeholder.component("distance", Component.text(onlinePlayer.getLocation().distance(location)))));
                found = true;
            }
        }
        if (!found) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().nearNoPlayersMessage,
                    Placeholder.component("radius", radiusComponent)));
        }
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        return super.onTabCompleteExecute(sender, args);
    }
}

