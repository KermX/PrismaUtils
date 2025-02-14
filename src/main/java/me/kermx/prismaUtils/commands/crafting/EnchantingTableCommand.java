package me.kermx.prismaUtils.commands.crafting;

import me.kermx.prismaUtils.commands.BaseCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class EnchantingTableCommand extends BaseCommand {


    public EnchantingTableCommand() {
        super("prismautils.command.enchantingtable", false, "/enchantingtable");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length > 0) {
            return false;
        }
        Player player = (Player) sender;
        Location location = player.getLocation();
        player.openEnchanting(location, true);
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        return super.onTabCompleteExecute(sender, args);
    }
}
