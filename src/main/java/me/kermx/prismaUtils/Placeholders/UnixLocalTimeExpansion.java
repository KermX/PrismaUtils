package me.kermx.prismaUtils.Placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kermx.prismaUtils.Utils.TimeZoneInfo;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UnixLocalTimeExpansion extends PlaceholderExpansion {

    private final Map<UUID, ZoneId> timezoneCache = new HashMap<>();

    @Override
    public @NotNull String getIdentifier() {
        return "unixlocaltime";
    }

    @Override
    public @NotNull String getAuthor() {
        return "KermX";
    }

    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String identifier) {
        String[] parts = identifier.split("_", 2);
        if (parts.length < 2) {
            return null;
        }

        String formatType = parts[0];
        String timestampStr = parts[1];

        long epochSeconds;
        try {
            epochSeconds = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            return null;
        }

        ZoneId playerZone = getPlayerTimeZone(offlinePlayer);
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, playerZone);

        return switch (formatType.toLowerCase()) {
            case "relative" -> formatRelative(zonedDateTime, ZonedDateTime.now(playerZone));
            case "time" -> formatTimeOnly(zonedDateTime);
            case "datetime" -> formatDateTime(zonedDateTime);
            case "weekdaydatetime" -> formatWeekdayDateTime(zonedDateTime);
            default -> null;
        };
    }

    private ZoneId getPlayerTimeZone(OfflinePlayer offlinePlayer) {
        ZoneId cached = timezoneCache.get(offlinePlayer.getUniqueId());
        if (cached != null) {
            return cached;
        }

        // catch possible failures
        if (!offlinePlayer.isOnline()) {
            return ZoneId.of("America/New_York");
        }
        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return ZoneId.of("America/New_York");
        }
        InetSocketAddress address = player.getAddress();
        if (address == null) {
            return ZoneId.of("America/New_York");
        }

        try {
            TimeZoneInfo tzInfo = new TimeZoneInfo(address);
            ZoneId zone = tzInfo.getZoneId();
            if (zone == null) {
                zone = ZoneId.of("America/New_York");
            }
            timezoneCache.put(player.getUniqueId(), zone);
            return zone;
        } catch (IOException e) {
            return ZoneId.of("America/New_York");
        }
    }

    private String formatRelative(ZonedDateTime targetTime, ZonedDateTime now) {
        Duration duration = Duration.between(now, targetTime);
        long seconds = duration.getSeconds();

        if (seconds == 0) {
            return "now";
        }

        boolean inFuture = seconds > 0;
        long absSeconds = Math.abs(seconds);

        long days = absSeconds / 86400;
        long hours = (absSeconds % 86400) / 3600;
        long minutes = ((absSeconds % 86400) % 3600) / 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" day").append(days > 1 ? "s" : "");
        else if (hours > 0) sb.append(hours).append(" hour").append(hours > 1 ? "s" : "");
        else if (minutes > 0) sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
        else sb.append(absSeconds).append(" second").append(absSeconds > 1 ? "s" : "");

        if (inFuture) {
            sb.insert(0, "in ");
        } else {
            sb.append(" ago");
        }
        return sb.toString();
    }

    private String formatTimeOnly(ZonedDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm z");
        return dateTime.format(formatter);
    }

    private String formatDateTime(ZonedDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");
        return dateTime.format(formatter);
    }

    private String formatWeekdayDateTime(ZonedDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy HH:mm z");
        return dateTime.format(formatter);
    }
}