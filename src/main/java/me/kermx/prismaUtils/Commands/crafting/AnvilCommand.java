package me.kermx.prismaUtils.Commands.crafting;

import me.kermx.prismaUtils.Commands.BaseCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AnvilCommand extends BaseCommand {

    public AnvilCommand(){
        super("prismautils.command.anvil", false, "/anvil");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length > 0){
            return false;
        }
        Player player = (Player) sender;
        Location location = player.getLocation();
        player.openAnvil(location, true);
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        return super.onTabCompleteExecute(sender, args);
    }
}
