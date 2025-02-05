package me.kermx.prismaUtils.handlers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.kermx.prismaUtils.managers.ConfigManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AfkTitlesHandler extends BukkitRunnable {

    private final Map<UUID, String[]> playerTitles = new HashMap<>();

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()){
            boolean isAfk = isPlayerAfk(player);
            UUID uuid = player.getUniqueId();

            if (isAfk){
                if (!playerTitles.containsKey(uuid)){
                    playerTitles.put(uuid, new String[]{getRandomTitle(), getRandomSubtitle()});
                }

                String[] assignedTitle = playerTitles.get(uuid);
                sendAfkTitle(player, assignedTitle[0], assignedTitle[1]);
            } else {
                playerTitles.remove(uuid);
                clearTitle(player);
            }

        }
    }

    private boolean isPlayerAfk(Player player){
        String afkStatus = PlaceholderAPI.setPlaceholders(player, ConfigManager.getInstance().afkPlaceholder);
        return afkStatus.equalsIgnoreCase("true");
    }

    private String getRandomTitle(){
        return ConfigManager.getInstance().afkTitles.get((int) (Math.random() * ConfigManager.getInstance().afkTitles.size()));
    }

    private String getRandomSubtitle(){
        return ConfigManager.getInstance().afkSubtitles.get((int) (Math.random() * ConfigManager.getInstance().afkSubtitles.size()));
    }

    private void sendAfkTitle(Player player, String title, String subtitle){
        Title formattedTitle = Title.title(
                MiniMessage.miniMessage().deserialize(title),
                MiniMessage.miniMessage().deserialize(subtitle),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ZERO)
        );
        player.showTitle(formattedTitle);
    }

    private void clearTitle(Player player){
        player.clearTitle();
    }
}
