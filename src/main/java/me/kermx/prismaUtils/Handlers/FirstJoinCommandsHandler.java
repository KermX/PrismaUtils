package me.kermx.prismaUtils.Handlers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.Utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FirstJoinCommandsHandler implements Listener {

    private final PrismaUtils plugin;

    public FirstJoinCommandsHandler(PrismaUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!ConfigUtils.getInstance().firstJoinCommandsEnabled) return;

        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) return;


        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (String command : ConfigUtils.getInstance().firstJoinCommands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, command));
            }
        }, 20L);
    }
}
