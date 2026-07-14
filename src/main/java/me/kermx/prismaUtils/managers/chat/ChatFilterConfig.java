package me.kermx.prismaUtils.managers.chat;

import me.kermx.prismaUtils.managers.config.ChatConfigManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record ChatFilterConfig(
        boolean enabled,
        Permissions permissions,
        Normalization normalization,
        UnicodePolicy unicodePolicy,
        Views views,
        Commands commands,
        Enforcement enforcement,
        Heuristics heuristics,
        Observability observability,
        Wordlists wordlists
) {
    public record Permissions(String bypassPerm, String notifyPerm) {}

    public record Normalization(
            boolean nfkcCaseFold,
            boolean removeInvisibles,
            int maxRepeat,
            boolean separatorFolding,
            boolean confusableFolding,
            boolean latinSkeletonFolding
    ) {}

    public record UnicodePolicy(
            UnicodeMode mode,
            String allowedExtraChars
    ) {}

    public enum UnicodeMode {
        OFF,
        ASCII_STRICT,
        SCRIPT_WHITELIST
    }

    public record Views(WordlistsViews wordlists) {
        public record WordlistsViews(
                List<String> allow,
                List<String> block,
                List<String> blockEmbedded,
                List<String> flag
        ) {}
    }

    public record Commands(
            boolean enabled,
            List<String> monitoredRoots,
            Roots roots
    ) {
        public record Roots(List<String> pmTwoArgs, List<String> pmOneArg, List<String> channelRest) {}
    }

    public record Enforcement(
            int cooldownSecondsOnViolation,
            boolean notifyPlayerOnBlock,
            String notifyPlayerMessage
    ) {}

    public record Heuristics(
            boolean enabled,
            RateLimit rateLimit,
            Duplicate duplicate,
            Links links
    ) {
        public record RateLimit(boolean enabled, int capacity, double refillPerSecond, Action action) {}
        public record Duplicate(boolean enabled, int windowSeconds, Action action) {}
        public record Links(boolean enabled, boolean detectUrls, boolean detectIp, boolean detectDiscord, Action action) {}
    }

    public record Observability(
            boolean logViolations,
            boolean logFlags,
            boolean includeNormalizedViews
    ) {}

    public record Wordlists(
            List<String> allow,
            List<String> block,
            List<String> blockEmbedded,
            List<String> flag
    ) {}

    public enum Action { ALLOW, FLAG, BLOCK }

    public static ChatFilterConfig loadFromChatConfig(JavaPlugin plugin, ChatConfigManager chatConfigManager) {
        FileConfiguration chatFile = chatConfigManager != null ? chatConfigManager.getConfig() : null;
        return loadFromSection(plugin, chatFile == null ? null : chatFile.getConfigurationSection("chat_filter"));
    }

    private static ChatFilterConfig loadFromSection(JavaPlugin plugin, ConfigurationSection root) {
        if (root == null) {
            return new ChatFilterConfig(
                    false,
                    new Permissions("prismautils.chatfilter.bypass", "prismautils.chatfilter.notify"),
                    new Normalization(true, true, 2, true, true, true),
                    new UnicodePolicy(UnicodeMode.SCRIPT_WHITELIST, "£§"),
                    new Views(new Views.WordlistsViews(
                            List.of("spaced", "compact"),
                            List.of("spaced", "compact"),
                            List.of("compact"),
                            List.of("spaced", "compact")
                    )),
                    new Commands(true, List.of(), new Commands.Roots(List.of(), List.of(), List.of())),
                    new Enforcement(5, true, "<red>Your message was filtered and not sent.</red>"),
                    new Heuristics(true,
                            new Heuristics.RateLimit(true, 6, 1.0, Action.BLOCK),
                            new Heuristics.Duplicate(true, 10, Action.BLOCK),
                            new Heuristics.Links(true, true, true, true, Action.BLOCK)
                    ),
                    new Observability(true, true, true),
                    new Wordlists(List.of(), List.of(), List.of(), List.of())
            );
        }

        boolean enabled = root.getBoolean("enabled", true);

        ConfigurationSection perms = root.getConfigurationSection("permissions");
        Permissions permissions = new Permissions(
                perms != null ? perms.getString("bypass_perm", "prismautils.chatfilter.bypass") : "prismautils.chatfilter.bypass",
                perms != null ? perms.getString("notify_perm", "prismautils.chatfilter.notify") : "prismautils.chatfilter.notify"
        );

        ConfigurationSection norm = root.getConfigurationSection("normalization");
        Normalization normalization = new Normalization(
                norm == null || norm.getBoolean("nfkc_case_fold", true),
                norm == null || norm.getBoolean("remove_invisibles", true),
                norm == null ? 2 : norm.getInt("max_repeat", 2),
                norm == null || norm.getBoolean("separator_folding", true),
                norm == null || norm.getBoolean("confusable_folding", true),
                norm == null || norm.getBoolean("latin_skeleton_folding", true)
        );

        ConfigurationSection up = root.getConfigurationSection("unicode_policy");
        UnicodePolicy unicodePolicy = new UnicodePolicy(
                parseUnicodeMode(up, "mode", UnicodeMode.SCRIPT_WHITELIST),
                up == null ? "£§" : up.getString("allowed_extra_chars", "£§")
        );

        ConfigurationSection viewsSec = root.getConfigurationSection("views.wordlists");
        List<String> allowViews = validateViews(plugin, "views.wordlists.allow",
                viewsSec == null ? List.of("spaced", "compact") : viewsSec.getStringList("allow"));
        List<String> blockViews = validateViews(plugin, "views.wordlists.block",
                viewsSec == null ? List.of("spaced", "compact") : viewsSec.getStringList("block"));
        List<String> flagViews = validateViews(plugin, "views.wordlists.flag",
                viewsSec == null ? List.of("spaced", "compact") : viewsSec.getStringList("flag"));
        List<String> blockEmbeddedRaw = (viewsSec == null) ? List.of("compact") : viewsSec.getStringList("block_embedded");
        List<String> blockEmbeddedViews = validateViews(plugin, "views.wordlists.block_embedded", blockEmbeddedRaw);
        if (blockEmbeddedViews.isEmpty()) blockEmbeddedViews = List.of("compact");

        Views views = new Views(new Views.WordlistsViews(
                allowViews,
                blockViews,
                blockEmbeddedViews,
                flagViews
        ));

        ConfigurationSection cmdSec = root.getConfigurationSection("commands");
        ConfigurationSection cmdRootsSec = root.getConfigurationSection("commands.roots");
        Commands commands = new Commands(
                cmdSec == null || cmdSec.getBoolean("enabled", true),
                cmdSec == null ? List.of() : cmdSec.getStringList("monitored_roots"),
                new Commands.Roots(
                        cmdRootsSec == null ? List.of() : cmdRootsSec.getStringList("pm_two_args"),
                        cmdRootsSec == null ? List.of() : cmdRootsSec.getStringList("pm_one_arg"),
                        cmdRootsSec == null ? List.of() : cmdRootsSec.getStringList("channel_rest")
                )
        );

        ConfigurationSection enf = root.getConfigurationSection("enforcement");
        Enforcement enforcement = new Enforcement(
                enf == null ? 5 : enf.getInt("cooldown_seconds_on_violation", 5),
                enf == null || enf.getBoolean("notify_player_on_block", true),
                enf == null
                        ? "<red>Your message was filtered and not sent.</red>"
                        : enf.getString("notify_player_message", "<red>Your message was filtered and not sent.</red>")
        );

        ConfigurationSection heur = root.getConfigurationSection("heuristics");
        Heuristics heuristics = new Heuristics(
                heur == null || heur.getBoolean("enabled", true),
                new Heuristics.RateLimit(
                        heur != null && heur.getBoolean("rate_limit.enabled", true),
                        heur == null ? 6 : heur.getInt("rate_limit.capacity", 6),
                        heur == null ? 1.0 : heur.getDouble("rate_limit.refill_per_second", 1.0),
                        parseAction(heur, "rate_limit.action", Action.BLOCK)
                ),
                new Heuristics.Duplicate(
                        heur != null && heur.getBoolean("duplicate.enabled", true),
                        heur == null ? 10 : heur.getInt("duplicate.window_seconds", 10),
                        parseAction(heur, "duplicate.action", Action.BLOCK)
                ),
                new Heuristics.Links(
                        heur != null && heur.getBoolean("links.enabled", true),
                        heur == null || heur.getBoolean("links.detect_urls", true),
                        heur == null || heur.getBoolean("links.detect_ip", true),
                        heur == null || heur.getBoolean("links.detect_discord", true),
                        parseAction(heur, "links.action", Action.BLOCK)
                )
        );

        ConfigurationSection obs = root.getConfigurationSection("observability");
        Observability observability = new Observability(
                obs == null || obs.getBoolean("log_violations", true),
                obs == null || obs.getBoolean("log_flags", true),
                obs == null || obs.getBoolean("include_normalized_views", true)
        );

        ConfigurationSection wl = root.getConfigurationSection("wordlists");
        Wordlists wordlists = new Wordlists(
                wl == null ? List.of() : wl.getStringList("allow"),
                wl == null ? List.of() : wl.getStringList("block"),
                wl == null ? List.of() : wl.getStringList("block_embedded"),
                wl == null ? List.of() : wl.getStringList("flag")
        );

        return new ChatFilterConfig(
                enabled,
                permissions,
                normalization,
                unicodePolicy,
                views,
                commands,
                enforcement,
                heuristics,
                observability,
                wordlists
        );
    }

    private static Action parseAction(ConfigurationSection heuristicsRoot, String path, Action def) {
        if (heuristicsRoot == null) return def;
        String raw = heuristicsRoot.getString(path);
        if (raw == null || raw.isBlank()) return def;
        try {
            return Action.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return def;
        }
    }

    private static UnicodeMode parseUnicodeMode(ConfigurationSection sec, String path, UnicodeMode def) {
        if (sec == null) return def;
        String raw = sec.getString(path);
        if (raw == null || raw.isBlank()) return def;
        try {
            return UnicodeMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return def;
        }
    }

    private static List<String> validateViews(JavaPlugin plugin, String path, List<String> raw) {
        if (raw == null || raw.isEmpty()) return List.of();

        List<String> out = new ArrayList<>();
        for (String s : raw) {
            if (s == null) continue;
            String v = s.trim().toLowerCase(Locale.ROOT);
            if (v.isBlank()) continue;

            if (v.equals("visible") || v.equals("spaced") || v.equals("compact")) {
                out.add(v);
            } else {
                plugin.getLogger().warning("[ChatFilter] Invalid view '" + s + "' at " + path + " (allowed: visible, spaced, compact)");
            }
        }
        return List.copyOf(out);
    }
}