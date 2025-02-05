package me.kermx.prismaUtils.Commands.player;

import me.kermx.prismaUtils.Commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HealCommand extends BaseCommand {

    public HealCommand(){
        super("prismautils.command.heal", true, "/heal [player|all]");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length == 0){
            if (!(sender instanceof Player player)){
                sender.sendMessage("You must specify a player name or use \"all\" from the console!");
                return true;
            }
            healPlayer(player);
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().healMessage));
            return true;
        }
        if (args.length == 1){
            String targetName = args[0];
            if (targetName.equalsIgnoreCase("all")){
                if (!sender.hasPermission("prismautils.command.heal.all")){
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().noPermissionMessage));
                    return true;
                }
                for (Player online : Bukkit.getOnlinePlayers()){
                    healPlayer(online);
                }
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().healAllMessage));
                return true;
            }
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null){
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().playerNotFoundMessage));
                return true;
            }
            if (!sender.hasPermission("prismautils.command.heal.others")){
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().noPermissionMessage));
                return true;
            }
            healPlayer(target);
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    ConfigManager.getInstance().healOtherMessage,
                    Placeholder.component("target", target.displayName())
            ));
            target.sendMessage(MiniMessage.miniMessage().deserialize(
                    ConfigManager.getInstance().healHealedByOtherMessage,
                    Placeholder.component("source", sender.name())
            ));
            return true;
        }
        return false;
    }

    private void healPlayer(Player player) {
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(20.0F); // Max saturation
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partialArg = args[0].toLowerCase();

            if ("all".startsWith(partialArg)) {
                completions.add("all");
            }
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

