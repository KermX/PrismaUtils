package me.kermx.prismaUtils.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kermx.prismaUtils.managers.general.ConfigManager;
import me.kermx.prismaUtils.managers.general.configs.EventPlaceholderConfigManager;
import me.kermx.prismaUtils.managers.general.configs.EventPlaceholderConfigManager.EventData;
import me.kermx.prismaUtils.utils.TimeUtils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;


public class EventPlaceholderExpansion extends PlaceholderExpansion {

    private final EventPlaceholderConfigManager eventPlaceholderConfig;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public EventPlaceholderExpansion() {
        this.eventPlaceholderConfig = ConfigManager.getInstance().getEventPlaceholdersConfig();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "event";
    }

    @Override
    public @NotNull String getAuthor() {
        return "KermX";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (!eventPlaceholderConfig.enabled) {
            return null;
        }

        // Check cache first
        CacheEntry cached = cache.get(params);
        if (cached != null && !cached.isExpired()) {
            return cached.getValue();
        }

        String result = resolveEventPlaceholder(params);

        // Cache the result
        if (result != null && eventPlaceholderConfig.cacheDuration > 0) {
            cache.put(params, new CacheEntry(result, eventPlaceholderConfig.cacheDuration));
        }

        return result;
    }

    private String resolveEventPlaceholder(String params) {
        // Format: eventKey_property
        // Examples: event1_name, event1_time_until, event1_time_remaining
        String[] parts = params.split("_", 2);
        if (parts.length < 2) {
            return null;
        }

        String eventKey = parts[0];
        String property = parts[1];

        if (!eventPlaceholderConfig.hasEvent(eventKey)) {
            return null;
        }

        EventData event = eventPlaceholderConfig.getEvent(eventKey);

        // Handle time-based placeholders
        if (property.startsWith("time_")) {
            return resolveTimePlaceholder(event, property.substring(5)); // Remove "time_" prefix
        }

        // Handle regular properties
        return resolveEventProperty(event, property);
    }

    private String resolveEventProperty(EventData event, String property) {
        switch (property.toLowerCase()) {
            case "name":
                return event.getName();
            case "description":
                return event.getDescription();
            case "start_date":
            case "startdate":
                return event.getStartDate();
            case "end_date":
            case "enddate":
                return event.getEndDate();
            case "location":
                return event.getLocation();
            case "host":
                return event.getHost();
            case "max_participants":
            case "maxparticipants":
                return String.valueOf(event.getMaxParticipants());
            case "rewards":
                return event.getRewards();
            case "status":
                return event.getStatus();
            default:
                return null;
        }
    }

    private String resolveTimePlaceholder(EventData event, String timeType) {
        String startDate = event.getStartDate();
        String endDate = event.getEndDate();

        // Handle TBD dates
        if ("TBD".equalsIgnoreCase(startDate) && timeType.equals("until")) {
            return "TBD";
        }
        if ("TBD".equalsIgnoreCase(endDate) && (timeType.equals("remaining") || timeType.equals("duration"))) {
            return "TBD";
        }

        try {
            LocalDateTime now = LocalDateTime.now();

            switch (timeType.toLowerCase()) {
                case "until":
                    LocalDateTime start = TimeUtils.parseDate(startDate);
                    return TimeUtils.calculateTimeUntil(start, now);

                case "remaining":
                    LocalDateTime end = TimeUtils.parseDate(endDate);
                    return TimeUtils.calculateTimeRemaining(end, now);

                case "duration":
                    if ("TBD".equalsIgnoreCase(startDate) || "TBD".equalsIgnoreCase(endDate)) {
                        return "TBD";
                    }
                    LocalDateTime startTime = TimeUtils.parseDate(startDate);
                    LocalDateTime endTime = TimeUtils.parseDate(endDate);
                    return TimeUtils.calculateDuration(startTime, endTime);

                default:
                    return null;
            }
        } catch (Exception e) {
            if (eventPlaceholderConfig.debug) {
                System.err.println("Error calculating time for event: " + e.getMessage());
            }
            return "Invalid date";
        }
    }

    public void clearCache() {
        cache.clear();
    }

    public void clearExpiredCache() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private static class CacheEntry {
        private final String value;
        private final long expireTime;

        public CacheEntry(String value, int cacheDurationSeconds) {
            this.value = value;
            this.expireTime = System.currentTimeMillis() + (cacheDurationSeconds * 1000L);
        }

        public String getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
}