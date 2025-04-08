package me.kermx.prismaUtils.commands.crafting;

import me.kermx.prismaUtils.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GrindstoneCommand extends BaseCommand {

    public GrindstoneCommand() {
        super("prismautils.command.grindstone", true, "/grindstone");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length > 1) {
            return false;
        }

        Player targetPlayer;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                return false;
            }
            targetPlayer = (Player) sender;
        } else {
            if (!(sender.hasPermission("prismautils.command.grindstone.others"))) {
                return false;
            }
            targetPlayer = sender.getServer().getPlayerExact(args[0]);
            if (targetPlayer == null) {
                return false;
            }
        }

        Location location = targetPlayer.getLocation();
        targetPlayer.openGrindstone(location, true);

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("prismautils.command.grindstone.others")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

}