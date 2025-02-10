package me.kermx.prismaUtils.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class TextUtils {
    private TextUtils() {}
    /**
     * Deserialize a string into a Component
     * @param string The string to deserialize
     * @return The deserialized Component
     */
    public static Component deserializeString(String string){
        return MiniMessage.miniMessage().deserialize(string);
    }
    /**
     * Deserialize a string into a Component with placeholders
     * @param message The string to deserialize
     * @param placeholders The placeholders to use
     * @return The deserialized Component
     */
    public static Component deserializeString(String message, TagResolver... placeholders) {
        return MiniMessage.miniMessage().deserialize(message, placeholders);
    }
    /**
     * Format a duration in milliseconds into a human-readable string
     * @param millis The duration in milliseconds
     * @return The formatted duration
     */
    public static String formatDuration(long millis){
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
}
