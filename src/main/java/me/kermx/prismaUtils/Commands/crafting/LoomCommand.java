package me.kermx.prismaUtils.Commands.crafting;

import me.kermx.prismaUtils.Commands.BaseCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LoomCommand extends BaseCommand {

    public LoomCommand(){
        super("prismautils.command.loom", false, "/loom");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length > 0){
            return false;
        }
        Player player = (Player) sender;
        Location location = player.getLocation();
        player.openLoom(location, true);
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        return super.onTabCompleteExecute(sender, args);
    }
}
