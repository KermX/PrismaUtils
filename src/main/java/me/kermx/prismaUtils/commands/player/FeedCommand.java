package me.kermx.prismaUtils.commands.player;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FeedCommand extends BaseCommand {

    public FeedCommand(){
        super("prismautils.command.feed", true, "/feed [player|all]");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        if (args.length == 0){
            if (!(sender instanceof Player player)){
                sender.sendMessage("You must specify a player name or use \"all\" from the console!");
                return true;
            }
            feedPlayer(player);
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().feedMessage));
            return true;
        }
        if (args.length == 1){
            String targetName = args[0];
            if (targetName.equalsIgnoreCase("all")){
                if (!sender.hasPermission("prismautils.command.feed.all")){
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().noPermissionMessage));
                    return true;
                }
                for (Player online : Bukkit.getOnlinePlayers()){
                    feedPlayer(online);
                }
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().feedAllMessage));
                return true;
            }
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null){
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().playerNotFoundMessage));
                return true;
            }
            if (!sender.hasPermission("prismautils.command.feed.others")){
                sender.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().getMessagesConfig().noPermissionMessage));
                return true;
            }
            feedPlayer(target);
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    ConfigManager.getInstance().getMessagesConfig().feedOtherMessage,
                    Placeholder.component("target", target.displayName())
            ));
            target.sendMessage(MiniMessage.miniMessage().deserialize(
                    ConfigManager.getInstance().getMessagesConfig().feedFedByOtherMessage,
                    Placeholder.component("source", sender.name())
            ));
            return true;
        }
        return false;
    }

    private void feedPlayer(Player player) {
        player.setFoodLevel(20);
        player.setSaturation(20.0F);
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
