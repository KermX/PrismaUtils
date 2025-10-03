package me.kermx.prismaUtils.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class TimeUtils {

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    };

    private TimeUtils() {
        throw new UnsupportedOperationException("Utility class (TimeUtils) - cannot be instantiated");
    }

    /**
     * Parse a date string into LocalDateTime, trying multiple formats.
     * Supported formats:
     * - yyyy-MM-dd HH:mm:ss
     * - yyyy-MM-dd
     * - MM/dd/yyyy HH:mm:ss
     * - MM/dd/yyyy
     *
     * @param dateStr the date string to parse; must not be null
     * @return the parsed LocalDateTime
     * @throws IllegalArgumentException if the date string cannot be parsed
     */
    public static LocalDateTime parseDate(String dateStr) {
        Objects.requireNonNull(dateStr, "Date string cannot be null");

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                if (dateStr.contains(":")) {
                    return LocalDateTime.parse(dateStr, formatter);
                } else {
                    return LocalDateTime.parse(dateStr + " 00:00:00",
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
            } catch (Exception ignored) {
                // Try next formatter
            }
        }
        throw new IllegalArgumentException("Unable to parse date: " + dateStr);
    }

    /**
     * Format a duration in seconds to a human-readable string.
     * For example: "2 days, 3 hours", "5 minutes, 30 seconds"
     *
     * @param totalSeconds the duration in seconds; must not be negative
     * @return the formatted duration string
     * @throws IllegalArgumentException if totalSeconds is negative
     */
    public static String formatDuration(long totalSeconds) {
        if (totalSeconds < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }

        if (totalSeconds == 0) {
            return "0 seconds";
        }

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(" day").append(days != 1 ? "s" : "");
        }

        if (hours > 0) {
            if (result.length() > 0) result.append(", ");
            result.append(hours).append(" hour").append(hours != 1 ? "s" : "");
        }

        if (minutes > 0 && days == 0) {
            if (result.length() > 0) result.append(", ");
            result.append(minutes).append(" minute").append(minutes != 1 ? "s" : "");
        }

        if (seconds > 0 && days == 0 && hours == 0) {
            if (result.length() > 0) result.append(", ");
            result.append(seconds).append(" second").append(seconds != 1 ? "s" : "");
        }

        return result.toString();
    }

    /**
     * Format relative time between two ZonedDateTime objects.
     * Returns strings like "in 5 minutes", "2 hours ago", "now"
     *
     * @param targetTime the target time; must not be null
     * @param now        the current time; must not be null
     * @return the formatted relative time string
     */
    public static String formatRelativeTime(ZonedDateTime targetTime, ZonedDateTime now) {
        Objects.requireNonNull(targetTime, "Target time cannot be null");
        Objects.requireNonNull(now, "Current time cannot be null");

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
        if (days > 0) {
            sb.append(days).append(" day").append(days > 1 ? "s" : "");
        } else if (hours > 0) {
            sb.append(hours).append(" hour").append(hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
        } else {
            sb.append(absSeconds).append(" second").append(absSeconds > 1 ? "s" : "");
        }

        if (inFuture) {
            sb.insert(0, "in ");
        } else {
            sb.append(" ago");
        }
        return sb.toString();
    }

    /**
     * Calculate time until a future date.
     *
     * @param futureDate the future date; must not be null
     * @param now        the current date/time; must not be null
     * @return the formatted time until string, or "Event has started" if the date has passed
     */
    public static String calculateTimeUntil(LocalDateTime futureDate, LocalDateTime now) {
        Objects.requireNonNull(futureDate, "Future date cannot be null");
        Objects.requireNonNull(now, "Current date/time cannot be null");

        if (now.isAfter(futureDate)) {
            return "Event has started";
        }
        return formatDuration(ChronoUnit.SECONDS.between(now, futureDate));
    }

    /**
     * Calculate time remaining until a future date.
     *
     * @param endDate the end date; must not be null
     * @param now     the current date/time; must not be null
     * @return the formatted time remaining string, or "Event has ended" if the date has passed
     */
    public static String calculateTimeRemaining(LocalDateTime endDate, LocalDateTime now) {
        Objects.requireNonNull(endDate, "End date cannot be null");
        Objects.requireNonNull(now, "Current date/time cannot be null");

        if (now.isAfter(endDate)) {
            return "Event has ended";
        }
        return formatDuration(ChronoUnit.SECONDS.between(now, endDate));
    }

    /**
     * Calculate duration between two dates.
     *
     * @param startDate the start date; must not be null
     * @param endDate   the end date; must not be null
     * @return the formatted duration string
     */
    public static String calculateDuration(LocalDateTime startDate, LocalDateTime endDate) {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");

        return formatDuration(ChronoUnit.SECONDS.between(startDate, endDate));
    }
}
