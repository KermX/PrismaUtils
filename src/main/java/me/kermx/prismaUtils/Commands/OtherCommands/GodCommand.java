package me.kermx.prismaUtils.Commands.OtherCommands;

import me.kermx.prismaUtils.Utils.ConfigUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.UUID;

public class GodCommand implements CommandExecutor, Listener {

    private final HashSet<UUID> godPlayers = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        UUID playerUUID = player.getUniqueId();

        if (!player.hasPermission("prismautils.command.god")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().noPermissionMessage));
            return true;
        }

        if (godPlayers.contains(playerUUID)) {
            godPlayers.remove(playerUUID);
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().godDisabledMessage));
        } else {
            godPlayers.add(playerUUID);
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigUtils.getInstance().godEnabledMessage));
        }
        return true;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (godPlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
