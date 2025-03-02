package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.BaseCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class RoundRotationCommand extends BaseCommand {

    public RoundRotationCommand() {
        super("prismautils.command.roundrotation", false, "/roundrotation");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            return false;
        }
        Player player = (Player) sender;
        Location loc = player.getLocation();

        float oldYaw = loc.getYaw();
        float oldPitch = loc.getPitch();

        float newYaw = Math.round(oldYaw / 45f) * 45f;
        float newPitch = Math.round(oldPitch / 45f) * 45f;

        loc.setYaw(newYaw);
        loc.setPitch(newPitch);
        player.teleport(loc);

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}
