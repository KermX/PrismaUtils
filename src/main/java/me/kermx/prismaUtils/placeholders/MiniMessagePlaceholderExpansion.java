package me.kermx.prismaUtils.placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class MiniMessagePlaceholderExpansion extends PlaceholderExpansion implements Relational {

    private static final String RESET_AMPERSAND = "&r";
    private static final String RESET_SECTION = "§r";

    @Override
    public @NotNull String getIdentifier() {
        return "minify".toLowerCase(Locale.ROOT);
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
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        return convertPlaceholder(player, null, params);
    }

    @Override
    public String onPlaceholderRequest(Player source, Player target, String params) {
        return convertPlaceholder(source, target, params);
    }

    /**
     * Converts a legacy placeholder string into a MiniMessage formatted string.
     *
     * @param source the source OfflinePlayer (or Player)
     * @param target the target Player for relational placeholders (may be null)
     * @param params the placeholder parameters
     * @return the MiniMessage formatted string
     */
    private String convertPlaceholder(OfflinePlayer source, Player target, String params) {
        // Build the placeholder key (e.g. "%example%").
        String placeholderKey = "%" + params + "%";

        // Resolve the placeholder using PlaceholderAPI.
        String resolvedText = resolvePlaceholder(source, target, placeholderKey);

        // If the result is simply a reset command, return a special token.
        if (isReset(resolvedText)) {
            return "<reset>";
        }

        // Auto-detect which legacy serializer to use based on the resolved text.
        LegacyComponentSerializer serializer = getLegacySerializer(resolvedText);

        // Deserialize the legacy formatted string into a Component.
        Component component = serializer.deserialize(resolvedText);

        // Serialize the Component to a MiniMessage formatted string.
        return TextUtils.serializeToMini(component);
    }

    /**
     * Resolves the placeholder using PlaceholderAPI.
     *
     * @param source         the source OfflinePlayer (or Player)
     * @param target         the target Player for relational placeholders (may be null)
     * @param placeholderKey the placeholder key in the form "%key%"
     * @return the resolved placeholder string
     */
    private String resolvePlaceholder(OfflinePlayer source, Player target, String placeholderKey) {
        if (target != null && source instanceof Player) {
            return PlaceholderAPI.setRelationalPlaceholders((Player) source, target, placeholderKey);
        }
        return PlaceholderAPI.setPlaceholders(source, placeholderKey);
    }

    /**
     * Checks if the provided result indicates a reset command.
     *
     * @param result the result string from placeholder resolution
     * @return true if the result is a reset, false otherwise.
     */
    private boolean isReset(String result) {
        return RESET_AMPERSAND.equals(result) || RESET_SECTION.equals(result);
    }

    /**
     * Auto-detects which legacy serializer to use based on whether the resolved text contains a section sign.
     *
     * @param text the legacy formatted string
     * @return the appropriate LegacyComponentSerializer
     */
    private LegacyComponentSerializer getLegacySerializer(String text) {
        return text.contains("§")
                ? LegacyComponentSerializer.legacySection()
                : LegacyComponentSerializer.legacyAmpersand();
    }
}