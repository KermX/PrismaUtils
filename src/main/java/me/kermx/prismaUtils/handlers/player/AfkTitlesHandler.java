package me.kermx.prismaUtils.handlers.player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.kermx.prismaUtils.managers.general.ConfigManager;
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
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean isAfk = isPlayerAfk(player);
            UUID uuid = player.getUniqueId();

            if (isAfk) {
                if (!playerTitles.containsKey(uuid)) {
                    playerTitles.put(uuid, new String[]{getRandomTitle(), getRandomSubtitle()});
                }

                String[] assignedTitle = playerTitles.get(uuid);
                sendAfkTitle(player, assignedTitle[0], assignedTitle[1]);
            } else {
                playerTitles.remove(uuid);
            }

        }
    }

    private boolean isPlayerAfk(Player player) {
        String afkStatus = PlaceholderAPI.setPlaceholders(player, ConfigManager.getInstance().getMessagesConfig().afkPlaceholder);
        return afkStatus.equalsIgnoreCase("true");
    }

    private String getRandomTitle() {
        return ConfigManager.getInstance().getMessagesConfig().afkTitles.get((int) (Math.random() * ConfigManager.getInstance().getMessagesConfig().afkTitles.size()));
    }

    private String getRandomSubtitle() {
        return ConfigManager.getInstance().getMessagesConfig().afkSubtitles.get((int) (Math.random() * ConfigManager.getInstance().getMessagesConfig().afkSubtitles.size()));
    }

    private void sendAfkTitle(Player player, String title, String subtitle) {
        Title formattedTitle = Title.title(
                MiniMessage.miniMessage().deserialize(title),
                MiniMessage.miniMessage().deserialize(subtitle),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(5), Duration.ZERO)
        );
        player.showTitle(formattedTitle);
    }
}
