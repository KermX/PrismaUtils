package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SmiteCommand extends BaseCommand {

    public SmiteCommand() {
        super("prismautils.command.smite", true, "/smite <player>");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }
        Player target = PlayerUtils.getOnlinePlayer(args[0]);

        if (target == null) {
            sender.sendMessage(TextUtils.deserializeString(ConfigManager.getInstance().getMessagesConfig().playerNotFoundMessage));
            return false;
        }

        if (args.length >= 2) {
            double damage;
            try {
                damage = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                return false;
            }

            target.getWorld().strikeLightningEffect(target.getLocation());
            target.damage(damage);
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().smiteMessageDamage,
                    Placeholder.component("damage", Component.text(damage)),
                    Placeholder.component("target", target.displayName())
            ));
        } else {
            target.getWorld().strikeLightningEffect(target.getLocation());
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().smiteMessageNoDamage,
                    Placeholder.component("target", target.displayName())
            ));
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