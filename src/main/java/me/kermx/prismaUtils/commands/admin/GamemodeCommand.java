package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GamemodeCommand extends BaseCommand {

    private final GameMode targetMode;
    private final String modeMane;

    public GamemodeCommand(GameMode targetMode, String permission, String usage) {
        super(permission, false, usage);
        this.targetMode = targetMode;
        this.modeMane = targetMode.name().toLowerCase();
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            setGamemode(player, targetMode);
            player.sendMessage(TextUtils.deserializeString("<green>Your gamemode has been set to <white>" + modeMane + "<green>!"));
            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("prismautils.command.gamemode.others")) {
                sender.sendMessage(TextUtils.deserializeString("<red>You don't have permission to change other player's gamemode!"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().playerNotFoundMessage)
                );
                return true;
            }

            setGamemode(target, targetMode);
            sender.sendMessage(TextUtils.deserializeString("<green>Gamemode of <white>" + target.getName() + "<green> has been set to <white>" + modeMane + "<green>!"));
            target.sendMessage(TextUtils.deserializeString("<green>Your gamemode has been set to <white>" + modeMane + "<green> by <white>" + sender.getName() + "<green>!"));
            return true;
        }
        return false;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        // First argument - player names
        if (args.length == 1 && sender.hasPermission("prismautils.command.gamemode.others")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private void setGamemode(Player player, GameMode mode) {
        player.setGameMode(mode);
    }
}
