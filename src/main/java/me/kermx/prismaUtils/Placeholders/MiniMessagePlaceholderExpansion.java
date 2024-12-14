package me.kermx.prismaUtils.Placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class MiniMessagePlaceholderExpansion extends PlaceholderExpansion implements Relational {
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
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        return convertToMiniMessage(player, null, params);
    }

    @Override
    public String onPlaceholderRequest(Player one, Player two, String identifier) {
        return convertToMiniMessage(one, two, identifier);
    }

    private String convertToMiniMessage(OfflinePlayer one, Player two, String params) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        if (params.toLowerCase(Locale.ROOT).startsWith("section_")){
            serializer = LegacyComponentSerializer.legacySection();
            params = params.substring(8);
        }

        String result = two != null && one instanceof Player ? PlaceholderAPI.setRelationalPlaceholders((Player) one, two, "%" + params + "%") : PlaceholderAPI.setPlaceholders(one, "%" + params + "%");
        if (result.equals("&r") || result.equals("§r")) return "<reset>";
        Component component = serializer.deserialize(result);
        return MiniMessage.miniMessage().serialize(component);
    }

}