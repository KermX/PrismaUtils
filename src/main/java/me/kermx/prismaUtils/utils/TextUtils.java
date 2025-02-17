package me.kermx.prismaUtils.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;

import java.util.Objects;
import java.util.StringJoiner;

public final class TextUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

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
        Objects.requireNonNull(message, "message cannot be null");
        return MINI_MESSAGE.deserialize(message);
    }

    /**
     * Deserialize a string into a Component with placeholders
     *
     * @param message      The string to deserialize
     * @param placeholders The placeholders to use
     * @return The deserialized Component
     */
    public static Component deserializeString(String message, TagResolver... placeholders) {
        Objects.requireNonNull(message, "message cannot be null");
        return MINI_MESSAGE.deserialize(message, placeholders);
    }

    /**
     * Safely deserializes a string into a Component. If the input is null, returns an empty Component.
     *
     * @param message the string to deserialize; may be null.
     * @return the deserialized Component, or an empty Component if message is null.
     */
    public static Component deserializeOrEmpty(String message) {
        return message == null ? Component.empty() : deserializeString(message);
    }

    /**
     * Serializes a Component into a legacy formatted string using the ampersand (&amp;) color code.
     *
     * @param component the Component to serialize; must not be null.
     * @return the legacy formatted string.
     */
    public static String serializeToLegacy(Component component) {
        Objects.requireNonNull(component, "Component cannot be null");
        return LEGACY_COMPONENT_SERIALIZER.serialize(component);
    }

    /**
     * Deserializes a legacy formatted string (with ampersand color codes) into a Component.
     *
     * @param legacyText the legacy formatted string; must not be null.
     * @return the deserialized Component.
     */
    public static Component deserializeLegacy(String legacyText) {
        Objects.requireNonNull(legacyText, "Legacy text cannot be null");
        return LEGACY_COMPONENT_SERIALIZER.deserialize(legacyText);
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
        long hours = minutes / 60;
        long days = hours / 24;

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

    /**
     * Normalize a material name by capitalizing the first letter of each word and replacing underscores with spaces.
     *
     * @param materialName the material name to normalize
     * @return the normalized material name
     */
    public static String normalizeMaterialName(String materialName) {
        String[] words = materialName.toLowerCase().split("_");
        StringBuilder normalized = new StringBuilder();
        for (String word : words) {
            normalized.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return normalized.toString().trim();
    }

    /**
     * Normalize a material name by capitalizing the first letter of each word and replacing underscores with spaces.
     *
     * @param material the material to normalize
     * @return the normalized material name
     */
    public static String normalizeMaterialName(Material material) {
        String materialName = material.name();
        String[] words = materialName.toLowerCase().split("_");
        StringBuilder normalized = new StringBuilder();
        for (String word : words) {
            normalized.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return normalized.toString().trim();
    }

}
