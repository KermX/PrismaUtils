package me.kermx.prismaUtils.utils;

import java.util.Map;
import java.util.Objects;
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
        Objects.requireNonNull(map, "map cannot be null");
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    /**
     * Inverts a map into a Map where each value maps to a List of keys.
     *
     * @param map the map to invert
     * @param <K> the key type
     * @param <V> the value type
     * @return a new map where each value maps to a list of keys that had that value in the original map
     */
    public static <K, V> Map<V, java.util.List<K>> invertMapMulti(Map<K, V> map) {
        Objects.requireNonNull(map, "map cannot be null");
        return map.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));
    }
}
