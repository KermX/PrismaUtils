package me.kermx.prismaUtils.managers.chat;

import java.util.Locale;

public record NormalizedMessage(
        String original,
        String visible,
        String spaced,
        String compact
) {
    public enum View { VISIBLE, SPACED, COMPACT }

    public String view(View v) {
        return switch (v) {
            case VISIBLE -> visible;
            case SPACED -> spaced;
            case COMPACT -> compact;
        };
    }

    public static View parseView(String raw) {
        if (raw == null) return null;
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "visible" -> View.VISIBLE;
            case "spaced" -> View.SPACED;
            case "compact" -> View.COMPACT;
            default -> null;
        };
    }
}