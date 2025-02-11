package me.kermx.prismaUtils.utils;

import java.util.Map;
import java.util.stream.Collectors;

public final class GenUtils {

    private GenUtils() {
        throw new UnsupportedOperationException("Utility class (GenUtils) - cannot be instantiated");
    }

    /**
     * Invert a map
     *
     * @param map The map to invert
     * @param <K> The key type
     * @param <V> The value type
     * @return The inverted map
     */
    public static <K, V> Map<V, K> invertMap(Map<K, V> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

}
