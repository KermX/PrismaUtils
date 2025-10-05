package me.kermx.prismaUtils.handlers.player;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import me.kermx.prismaUtils.managers.core.ConfigManager;
import me.kermx.prismaUtils.utils.TextUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class RespawnMessageHandler implements Listener {

    @EventHandler
    public void onRespawn(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();

        List<String> respawnMessages = ConfigManager.getInstance().getMessagesConfig().respawnMessages;

        if (respawnMessages != null && !respawnMessages.isEmpty()){
            for (String message : respawnMessages) {
                player.sendMessage(TextUtils.deserializeString(message));
            }
        }
    }
}
