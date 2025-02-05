package me.kermx.prismaUtils.Commands.CraftingStationCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CraftingTableCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("prismautils.command.craftingtable")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            return true;
        }

        Location location = player.getLocation();
        player.openWorkbench(location, true);
        return true;
    }
}