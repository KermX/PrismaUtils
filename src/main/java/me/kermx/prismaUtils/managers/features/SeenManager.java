package me.kermx.prismaUtils.managers.features;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SeenManager {

    private final Map<UUID, Long> loginTimes = new HashMap<>();

    public void recordLogin(UUID uuid, long loginTime) {
        loginTimes.put(uuid, loginTime);
    }

    public void clearLogin(UUID uuid) {
        loginTimes.remove(uuid);
    }

    public Long getLoginTime(UUID uuid) {
        return loginTimes.get(uuid);
    }

    public String formatDuration(long millis){
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringBuilder stringBuilder = new StringBuilder();
        if (days > 0) stringBuilder.append(days).append("d ");
        if (hours > 0) stringBuilder.append(hours).append("h ");
        if (minutes > 0) stringBuilder.append(minutes).append("m ");
        if (seconds > 0) stringBuilder.append(seconds).append("s");
        if (stringBuilder.isEmpty()) stringBuilder.append("0s");

        return stringBuilder.toString().trim();
    }

    public OfflinePlayer getOfflinePlayer(String name){
        return Bukkit.getOfflinePlayer(name);
    }
}
