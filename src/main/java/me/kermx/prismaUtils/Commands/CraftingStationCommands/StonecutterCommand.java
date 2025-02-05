package me.kermx.prismaUtils.Commands.CraftingStationCommands;

import me.kermx.prismaUtils.Commands.base.BaseCommand;
import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class StonecutterCommand extends BaseCommand {

    public StonecutterCommand(){
        super("prismautils.command.stonecutter", false, "/stonecutter");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length > 0){
            return false;
        }
        Player player = (Player) sender;
        Location location = player.getLocation();
        player.openStonecutter(location, true);
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        return super.onTabCompleteExecute(sender, args);
    }
}
