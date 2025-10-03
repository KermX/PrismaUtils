package me.kermx.prismaUtils.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class TimePlaceholderExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "time";
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
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        params = params.toLowerCase();

        // Day of week
        switch (params) {
            case "day_of_week" -> {
                return String.valueOf(today.getDayOfWeek().getValue()); // 1-7 (Monday-Sunday)
            }
            case "day_of_week_name" -> {
                return today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            }
            case "day_of_week_short" -> {
                return today.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            }


            // Day of month/year
            case "day_of_month" -> {
                return String.valueOf(today.getDayOfMonth()); // 1-31
            }
            case "day_of_year" -> {
                return String.valueOf(today.getDayOfYear()); // 1-366
            }


            // Week
            case "week_of_year" -> {
                return String.valueOf(today.get(WeekFields.ISO.weekOfWeekBasedYear()));
            }
            case "week_of_month" -> {
                return String.valueOf(today.get(WeekFields.ISO.weekOfMonth()));
            }


            // Month
            case "month" -> {
                return String.valueOf(today.getMonthValue()); // 1-12
            }
            case "month_name" -> {
                return today.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            }
            case "month_short" -> {
                return today.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            }


            // Year
            case "year" -> {
                return String.valueOf(today.getYear());
            }
            case "year_short" -> {
                return String.valueOf(today.getYear() % 100); // Last 2 digits
            }


            // Quarter
            case "quarter" -> {
                return String.valueOf((today.getMonthValue() - 1) / 3 + 1); // 1-4
            }


            // Time components
            case "hour" -> {
                return String.valueOf(currentTime.getHour()); // 0-23
            }
            case "hour_12" -> {
                int hour12 = currentTime.getHour() % 12;
                return String.valueOf(hour12 == 0 ? 12 : hour12); // 1-12
            }
            case "minute" -> {
                return String.format("%02d", currentTime.getMinute()); // 00-59
            }
            case "second" -> {
                return String.format("%02d", currentTime.getSecond()); // 00-59
            }
            case "millisecond" -> {
                return String.valueOf(currentTime.get(ChronoField.MILLI_OF_SECOND));
            }


            // AM/PM
            case "ampm" -> {
                return currentTime.getHour() < 12 ? "AM" : "PM";
            }
            case "ampm_lower" -> {
                return currentTime.getHour() < 12 ? "am" : "pm";
            }


            // Formatted dates
            case "date" -> {
                return today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            case "date_us" -> {
                return today.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            }
            case "date_eu" -> {
                return today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            case "date_long" -> {
                return today.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            }


            // Formatted times
            case "time_24" -> {
                return currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            }
            case "time_12" -> {
                return now.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
            }
            case "time_short" -> {
                return currentTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            }


            // Full datetime
            case "datetime" -> {
                return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            case "datetime_long" -> {
                return now.format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' hh:mm a"));
            }


            // ISO formats
            case "iso_date" -> {
                return today.format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
            case "iso_time" -> {
                return currentTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
            }
            case "iso_datetime" -> {
                return now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }


            // Unix timestamps
            case "unix" -> {
                return String.valueOf(Instant.now().getEpochSecond());
            }
            case "unix_millis" -> {
                return String.valueOf(Instant.now().toEpochMilli());
            }


            // Timezone
            case "timezone" -> {
                return ZoneId.systemDefault().getId();
            }
            case "timezone_offset" -> {
                ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(Instant.now());
                return offset.toString();
            }


            // Leap year check
            case "is_leap_year" -> {
                return String.valueOf(today.isLeapYear());
            }


            // Days in month
            case "days_in_month" -> {
                return String.valueOf(today.lengthOfMonth());
            }


            // Days in year
            case "days_in_year" -> {
                return String.valueOf(today.lengthOfYear());
            }


            // Start/End of periods
            case "start_of_week" -> {
                return today.with(ChronoField.DAY_OF_WEEK, 1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
            case "end_of_week" -> {
                return today.with(ChronoField.DAY_OF_WEEK, 7).format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
            case "start_of_month" -> {
                return today.withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
            case "end_of_month" -> {
                return today.withDayOfMonth(today.lengthOfMonth()).format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
            case "start_of_year" -> {
                return today.withDayOfYear(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
            case "end_of_year" -> {
                return today.withDayOfYear(today.lengthOfYear()).format(DateTimeFormatter.ISO_LOCAL_DATE);
            }


            // Time until/since specific points
            case "days_until_weekend" -> {
                int daysUntil = (6 - today.getDayOfWeek().getValue()) % 7;
                return String.valueOf(daysUntil == 0 ? 0 : daysUntil);
            }
            case "days_until_month_end" -> {
                return String.valueOf(today.lengthOfMonth() - today.getDayOfMonth());
            }
            case "days_until_year_end" -> {
                return String.valueOf(today.lengthOfYear() - today.getDayOfYear());
            }


            // Season (Northern Hemisphere)
            case "season" -> {
                return getSeason(today.getMonthValue());
            }
        }

        // Custom format support (e.g., %time_format_yyyy-MM-dd%)
        if (params.startsWith("format_")) {
            try {
                String pattern = params.substring(7).replace("-", "/");
                return now.format(DateTimeFormatter.ofPattern(pattern));
            } catch (Exception e) {
                return "Invalid format";
            }
        }

        return null;
    }

    private String getSeason(int month) {
        return switch (month) {
            case 12, 1, 2 -> "Winter";
            case 3, 4, 5 -> "Spring";
            case 6, 7, 8 -> "Summer";
            case 9, 10, 11 -> "Fall";
            default -> "Unknown";
        };
    }
}
