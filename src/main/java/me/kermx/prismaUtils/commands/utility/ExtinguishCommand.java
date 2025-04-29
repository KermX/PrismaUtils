package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ExtinguishCommand extends BaseCommand {

    public ExtinguishCommand() {
        super("prismautils.command.extinguish", false, "/extinguish");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length > 0) return false;

        if (player.getFireTicks() > 0){
            player.setFireTicks(0);
            player.sendMessage("You have been extinguished!");
            return true;
        }

        player.setFireTicks(0);
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}
