package me.kermx.prismaUtils.Commands.OtherCommands;

import me.kermx.prismaUtils.Commands.base.BaseCommand;
import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TopCommand extends BaseCommand {

    public TopCommand(){
        super("prismautils.command.top", false, "/top");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length > 0){
            return false;
        }
        Player player = (Player) sender;
        Location currentLocation = player.getLocation();
        Location topLocation = currentLocation.getWorld().getHighestBlockAt(currentLocation).getLocation().add(0.5, 1, 0.5);
        player.teleport(topLocation);
        player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().topMessage));
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        return super.onTabCompleteExecute(sender, args);
    }
}
