package me.kermx.prismaUtils.commands.player;

import me.kermx.prismaUtils.commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class GodCommand extends BaseCommand implements Listener {

    private final HashSet<UUID> godPlayers = new HashSet<>();

    public GodCommand() {
        super("prismautils.command.god", true, "/god [on|off] [player_name]");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            // Toggle god mode for the sender
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("You must specify a state and a player i.e. /god [on|off] [player_name]!");
                return false;
            }
            toggleGodMode((Player) sender);
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on")) {
                // Enable god mode for the sender
                setGodMode((Player) sender, true);
            } else if (args[0].equalsIgnoreCase("off")) {
                // Disable god mode for the sender
                setGodMode((Player) sender, false);
            } else {
                return false; // Incorrect usage
            }
        } else if (args.length == 2) {
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().playerNotFoundMessage)
                );
                return true;
            }

            if (args[0].equalsIgnoreCase("on")) {
                // Enable god mode for the target player
                setGodMode(targetPlayer, true);
                sender.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().godEnabledForPlayerMessage,
                        Placeholder.component("player", targetPlayer.displayName()))
                );
            } else if (args[0].equalsIgnoreCase("off")) {
                // Disable god mode for the target player
                setGodMode(targetPlayer, false);
                sender.sendMessage(TextUtils.deserializeString(
                        ConfigManager.getInstance().getMessagesConfig().godDisabledForPlayerMessage,
                        Placeholder.component("player", targetPlayer.displayName()))
                );
            } else {
                return false; // Incorrect usage
            }
        } else {
            return false; // Incorrect usage
        }
        return true;
    }

    private void toggleGodMode(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (godPlayers.contains(playerUUID)) {
            godPlayers.remove(playerUUID);
            player.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().godDisabledMessage)
            );
        } else {
            godPlayers.add(playerUUID);
            player.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().godEnabledMessage)
            );
        }
    }

    private void setGodMode(Player player, boolean enable) {
        UUID playerUUID = player.getUniqueId();
        if (enable) {
            godPlayers.add(playerUUID);
            player.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().godEnabledMessage)
            );
        } else {
            godPlayers.remove(playerUUID);
            player.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().godDisabledMessage)
            );
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (godPlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if ("on".startsWith(args[0].toLowerCase())) {
                completions.add("on");
            }
            if ("off".startsWith(args[0].toLowerCase())) {
                completions.add("off");
            }
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