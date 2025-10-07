package me.kermx.prismaUtils.commands.admin;

import me.kermx.prismaUtils.commands.core.BaseCommand;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.PlayerUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CuffCommand extends BaseCommand implements Listener {

    private static final Set<UUID> cuffedPlayers = new HashSet<>();

    public CuffCommand() {
        super("prismautils.command.cuff", true, "/cuff <player>");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args) {
        if (args.length != 1) {
            return false;
        }
        Player target = PlayerUtils.getOnlinePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().playerNotFoundMessage)
            );
            return false;
        }

        if (cuffedPlayers.contains(target.getUniqueId())) {
            cuffedPlayers.remove(target.getUniqueId());
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().cuffUncuffMessage,
                    Placeholder.component("target", target.displayName()))
            );
        } else {
            cuffedPlayers.add(target.getUniqueId());
            sender.sendMessage(TextUtils.deserializeString(
                    ConfigManager.getInstance().getMessagesConfig().cuffMessage,
                    Placeholder.component("target", target.displayName()))
            );
        }
        return true;
    }

    private static boolean isCuffed(Player player) {
        return cuffedPlayers.contains(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (isCuffed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isCuffed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (isCuffed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isCuffed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isCuffed(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandSendEvent event) {
        if (isCuffed(event.getPlayer())) {
            event.getCommands().clear();
        }
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return PlayerUtils.getOnlinePlayerNamesStartingWith(args[0]);
        }
        return super.onTabCompleteExecute(sender, args);
    }
}