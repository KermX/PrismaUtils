package me.kermx.prismaUtils.commands.player;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TopCommand extends BaseCommand {

    public TopCommand() {
        super("prismautils.command.top", false, "/top");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            return false;
        }

        Player player = (Player) sender;
        Location currentLocation = player.getLocation();
        Location topLocation = currentLocation.getWorld().getHighestBlockAt(currentLocation).getLocation().add(0.5, 1, 0.5);
        topLocation.setPitch(player.getPitch());
        topLocation.setYaw(player.getYaw());

        if (currentLocation.getY() >= topLocation.getY() - 1) { // Already at the top
            player.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().topMessageAlreadyAtTop)
            );
        } else {
            player.setFallDistance(0);
            player.teleport(topLocation);
            player.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().topMessage)
            );
        }

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}