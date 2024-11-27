package me.kermx.prismaUtils.Commands.OtherCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NearCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("prismautils.command.near")){
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            return true;
        }

        Location location = player.getLocation();
        double radius = 100;

        if (args.length > 0){
            try {
                radius = Double.parseDouble(args[0]);
            } catch (NumberFormatException e){
                player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().nearInvalidRadiusMessage));
                return true;
            }
        }

        Component radiusComponent = Component.text(radius);
        player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().nearNearPlayersMessage, Placeholder.component("radius", radiusComponent)));
        boolean found = false;
        for (Player onlinePlayer : player.getWorld().getPlayers()){
            if (onlinePlayer.getLocation().distance(location) <= radius && onlinePlayer != player){

                player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().nearNearbyPlayersMessage,
                        Placeholder.component("player", onlinePlayer.displayName()),
                        Placeholder.component("distance", Component.text(onlinePlayer.getLocation().distance(location)))));
                found = true;
            }
        }

        if (!found){
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().nearNoPlayersMessage,
                    Placeholder.component("radius", radiusComponent)));
        }
        return true;
    }
}
