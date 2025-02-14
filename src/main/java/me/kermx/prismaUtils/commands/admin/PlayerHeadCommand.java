package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.utils.ItemUtils;
import me.kermx.prismaUtils.utils.PlayerUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerHeadCommand extends BaseCommand {

    public PlayerHeadCommand() {
        super("prismautils.command.playerhead", false, "/playerhead <player>");
    }

    @Override
    public boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        Player player = (Player) sender;
        String target = args[0];

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();

        OfflinePlayer targetPlayer = PlayerUtils.getOfflinePlayer(target);

        headMeta.setOwningPlayer(targetPlayer);
        headMeta.displayName(Component.text(target + "'s Head"));
        head.setItemMeta(headMeta);

        ItemUtils.giveItems(player, head);
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String partialArg = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                String name = player.getName().toLowerCase();
                if (name.startsWith(partialArg)) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }
}
