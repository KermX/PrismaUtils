package me.kermx.prismaUtils.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.StringJoiner;

public final class TextUtils {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private TextUtils() {
        throw new UnsupportedOperationException("Utility class (TextUtils) - cannot be instantiated");
    }

    /**
     * Deserialize a string into a Component
     *
     * @param message The string to deserialize
     * @return The deserialized Component
     */
    public static Component deserializeString(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    /**
     * Deserialize a string into a Component with placeholders
     *
     * @param message The string to deserialize
     * @param placeholders The placeholders to use
     * @return The deserialized Component
     */
    public static Component deserializeString(String message, TagResolver... placeholders) {
        return MINI_MESSAGE.deserialize(message, placeholders);
    }

    /**
     * Format a duration in milliseconds into a human-readable string.
     * For example: "1d 2h 3m 4s".
     *
     * @param millis the duration in milliseconds; must not be negative
     * @return the formatted duration
     * @throws IllegalArgumentException if millis is negative
     */
    public static String formatDuration(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours   = minutes / 60;
        long days    = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringJoiner joiner = new StringJoiner(" ");
        if (days > 0) {
            joiner.add(days + "d");
        }
        if (hours > 0) {
            joiner.add(hours + "h");
        }
        if (minutes > 0) {
            joiner.add(minutes + "m");
        }
        if (seconds > 0 || joiner.length() == 0) {
            joiner.add(seconds + "s");
        }

        return joiner.toString();
    }

}
