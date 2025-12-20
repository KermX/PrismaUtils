package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class NightVisionCommand extends BaseCommand {

    public NightVisionCommand() {
        super("prismautils.command.nightvision", true, "/nightvision [on|off] [player_name]");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console must specify a state and a player i.e. /nightvision [on|off] [player_name]!");
                return true;
            }
            toggleNightVision(player);
            return true;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console must specify a player i.e. /nightvision [on|off] [player_name]!");
                return true;
            }

            if (args[0].equalsIgnoreCase("on")) {
                setNightVision(player, true);
                player.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().nightvisionEnabledMessage)
                );
                return true;
            }

            if (args[0].equalsIgnoreCase("off")) {
                setNightVision(player, false);
                player.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().nightvisionDisabledMessage)
                );
                return true;
            }

            return false;
        }

        if (args.length == 2) {
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().playerNotFoundMessage)
                );
                return true;
            }

            if (args[0].equalsIgnoreCase("on")) {
                setNightVision(targetPlayer, true);
                sender.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().nightvisionEnabledForPlayerMessage,
                        Placeholder.component("player", targetPlayer.displayName()))
                );
                return true;
            }

            if (args[0].equalsIgnoreCase("off")) {
                setNightVision(targetPlayer, false);
                sender.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().nightvisionDisabledForPlayerMessage,
                        Placeholder.component("player", targetPlayer.displayName()))
                );
                return true;
            }

            return false;
        }

        return false;
    }

    private void toggleNightVision(Player player) {
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().nightvisionDisabledMessage)
            );
            return;
        }

        setNightVision(player, true);
        player.sendMessage(TextUtils.deserializeString(
                ConfigManager.getInstance().getMessagesConfig().nightvisionEnabledMessage)
        );
    }

    private void setNightVision(Player player, boolean enable) {
        if (enable) {
            PotionEffect effect = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false);
            player.addPotionEffect(effect);
            return;
        }
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if ("on".startsWith(args[0].toLowerCase())) completions.add("on");
            if ("off".startsWith(args[0].toLowerCase())) completions.add("off");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off"))) {
            String partialName = args[1].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partialName)) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }
}