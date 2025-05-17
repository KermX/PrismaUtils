package me.kermx.prismaUtils.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kermx.prismaUtils.managers.features.AfkManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class AfkPlaceholderExpansion extends PlaceholderExpansion {

    private final AfkManager afkManager;

    public AfkPlaceholderExpansion(AfkManager afkManager) {
        this.afkManager = afkManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "afk";
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
        if (player == null || !player.isOnline()) {
            return "";
        }

        if (params.equalsIgnoreCase("status")) {
            return afkManager.isAfk(player.getUniqueId()) ? "AFK" : "Online";
        }

        if (params.equalsIgnoreCase("boolean") || params.equalsIgnoreCase("afk")) {
            return afkManager.isAfk(player.getUniqueId()) ? "true" : "false";
        }

        if (params.equalsIgnoreCase("symbol")) {
            return afkManager.isAfk(player.getUniqueId()) ? "⏰" : "";
        }

        if (params.equalsIgnoreCase("colored_status")) {
            return afkManager.isAfk(player.getUniqueId()) ?
                    "<gray>AFK</gray>" :
                    "<green>Online</green>";
        }

        return null;
    }
}


