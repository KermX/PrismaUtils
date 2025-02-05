package me.kermx.prismaUtils.Commands.player;

import me.kermx.prismaUtils.Commands.BaseCommand;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class GodCommand extends BaseCommand implements Listener {

    private final HashSet<UUID> godPlayers = new HashSet<>();

    public GodCommand(){
        super("prismautils.command.god", false, "/god");
    }

    @Override
    protected boolean onCommandExecute(CommandSender sender, String label, String[] args){
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (godPlayers.contains(playerUUID)){
            godPlayers.remove(playerUUID);
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().godDisabledMessage));
        } else {
            godPlayers.add(playerUUID);
            player.sendMessage(MiniMessage.miniMessage().deserialize(ConfigManager.getInstance().godEnabledMessage));
        }
        return true;
    }

    @Override
    protected List<String> onTabCompleteExecute(CommandSender sender, String[] args){
        return super.onTabCompleteExecute(sender, args);
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
