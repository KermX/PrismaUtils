package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.ItemUtils;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class PlayerHeadCommand extends BaseCommand {

    public PlayerHeadCommand() {
        super("prismautils.command.playerhead", true, "/playerhead <player> [player]");
    }

    @Override
    public boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }

        String headOwnerName = args[0];
        OfflinePlayer headOwner = PlayerUtils.getOfflinePlayer(headOwnerName);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(headOwner);
        headMeta.displayName(
                TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().playerHeadName,
                        Placeholder.component("player", Component.text(headOwnerName)))
        );
        head.setItemMeta(headMeta);

        Player recipient;
        if (sender instanceof Player) {
            if (args.length < 2) {
                recipient = (Player) sender;
            } else {
                recipient = Bukkit.getPlayer(args[1]);
                if (recipient == null) {
                    sender.sendMessage(
                            TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().playerNotFoundMessage)
                    );
                    return true;
                }
            }
        } else {
            if (args.length < 2) {
                sender.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().mustSpecifyPlayerMessage)
                );
                return true;
            }
            recipient = Bukkit.getPlayer(args[1]);
            if (recipient == null) {
                sender.sendMessage(
                        TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().playerNotFoundMessage)
                );
                return true;
            }
        }

        ItemUtils.giveItems(recipient, head);
        if (!(sender instanceof Player)) {
            sender.sendMessage(
                    TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().playerHeadGivenMessage,
                            Placeholder.component("player", Component.text(headOwnerName)),
                            Placeholder.component("recipient", Component.text(recipient.getName())))
            );
        }
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return PlayerUtils.getOnlinePlayerNamesStartingWith(args[0]);
        }
        return super.onTabCompleteExecute(sender, args);
    }
}