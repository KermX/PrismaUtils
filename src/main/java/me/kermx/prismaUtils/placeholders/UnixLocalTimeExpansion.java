package me.kermx.prismaUtils.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kermx.prismaUtils.external.TimeZoneInfo;
import me.kermx.prismaUtils.utils.TimeUtils;
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
    public boolean persist() {
        return true;
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
            case "relative" -> TimeUtils.formatRelativeTime(zonedDateTime, ZonedDateTime.now(playerZone));
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
