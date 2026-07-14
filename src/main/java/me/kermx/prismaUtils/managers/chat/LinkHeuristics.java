package me.kermx.prismaUtils.managers.chat;

import java.util.regex.Pattern;

public final class LinkHeuristics {
    private static final Pattern DOMAIN = Pattern.compile(
            "(?i)\\b((https?://)?(?=[a-z0-9.-]*[a-z][a-z0-9.-]*\\.[a-z]{2,}\\b)[a-z0-9-]+(?:\\.[a-z0-9-]+)*\\.[a-z]{2,})(/\\S*)?\\b"
    );

    private static final Pattern URL_WITH_SCHEME = Pattern.compile("(?i)\\bhttps?://\\S+\\b");
    private static final Pattern IPV4 = Pattern.compile("\\b(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\b");
    private static final Pattern DISCORD = Pattern.compile("(?i)\\b(discord\\.gg|discord\\.com/invite)\\b");

    private LinkHeuristics() {}

    public static String detect(NormalizedMessage msg, ChatFilterConfig.Heuristics.Links cfg) {
        String original = msg.original();
        String visible = msg.visible();

        if (cfg.detectDiscord() && DISCORD.matcher(visible).find()) return "discord_invite";
        if (cfg.detectIp() && IPV4.matcher(visible).find()) return "ip";

        if (cfg.detectUrls()) {
            if (original != null && DOMAIN.matcher(original).find()) return "url";
            if (URL_WITH_SCHEME.matcher(visible).find()) return "url";
        }

        return null;
    }
}