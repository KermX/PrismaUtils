package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.BaseCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CenterBlockCommand extends BaseCommand {
    public CenterBlockCommand() {
        super("prismautils.command.centerblock", false, "/centerblock");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            return false;
        }
        Player player = (Player) sender;
        Location loc = player.getLocation();

        double oldX = loc.getX();
        double oldZ = loc.getZ();

        double newX = Math.floor(oldX) + 0.5;
        double newZ = Math.floor(oldZ) + 0.5;

        loc.setX(newX);
        loc.setZ(newZ);
        player.teleport(loc);

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}
