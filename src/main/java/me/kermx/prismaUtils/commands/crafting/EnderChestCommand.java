package me.kermx.prismaUtils.commands.crafting;

import me.kermx.prismaUtils.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnderChestCommand extends BaseCommand {

    public EnderChestCommand() {
        super("prismautils.command.enderchest", true, "/enderchest");
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
            if (!sender.hasPermission("prismautils.command.enderchest.others")) {
                return false;
            }

            targetPlayer = Bukkit.getPlayerExact(args[0]);
            if (targetPlayer == null) {
                return false;
            }
        }

        Inventory enderChest = targetPlayer.getEnderChest();
        targetPlayer.openInventory(enderChest);

        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("prismautils.command.enderchest.others")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
