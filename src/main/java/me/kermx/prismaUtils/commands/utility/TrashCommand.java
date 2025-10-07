package me.kermx.prismaUtils.commands.utility;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrashCommand extends BaseCommand {

    public TrashCommand() {
        super("prismautils.command.trash", true, "/trash");
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
            if (!(sender.hasPermission("prismautils.command.trash.others"))) {
                return false;
            }
            targetPlayer = sender.getServer().getPlayerExact(args[0]);
            if (targetPlayer == null) {
                return false;
            }
        }

        Inventory trash = Bukkit.createInventory(null, 54, Component.text("Trash"));
        targetPlayer.openInventory(trash);

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("prismautils.command.trash.others")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
