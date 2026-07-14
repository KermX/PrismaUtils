package me.kermx.prismaUtils.managers.chat;

import me.kermx.prismaUtils.PrismaUtils;
import me.kermx.prismaUtils.utils.TextUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public final class ChatFilterService {

    public enum Channel { PUBLIC_CHAT, COMMAND_PRIVATE, COMMAND_CHANNEL, COMMAND_OTHER }

    public record Decision(
            ChatFilterConfig.Action action,
            String ruleId,
            String reason,
            String matched,
            String view,
            NormalizedMessage normalized,
            Channel channel
    ) {
        public boolean blocked() { return action == ChatFilterConfig.Action.BLOCK; }
        public boolean flagged() { return action == ChatFilterConfig.Action.FLAG; }

        public static Decision allow(NormalizedMessage nm, Channel channel) {
            return new Decision(ChatFilterConfig.Action.ALLOW, null, null, null, null, nm, channel);
        }
    }

    private final PrismaUtils plugin;
    private final MessageNormalizer normalizer = new MessageNormalizer();

    private ChatFilterConfig config;
    private KeywordMatcher allow;
    private KeywordMatcher block;
    private KeywordMatcher blockEmbedded;
    private KeywordMatcher flag;

    private final Map<UUID, Long> cooldownUntilMs = new HashMap<>();
    private final Map<UUID, TokenBucket> buckets = new HashMap<>();
    private final Map<UUID, LastMessage> lastMessage = new HashMap<>();

    private record LastMessage(String compact, long atMs) {}

    public ChatFilterService(PrismaUtils plugin, ChatFilterConfig config) {
        this.plugin = plugin;
        reload(config);
    }

    public void reload(ChatFilterConfig newConfig) {
        this.config = newConfig;

        this.allow = KeywordMatcher.fromPatterns(config.wordlists().allow());
        this.block = KeywordMatcher.fromPatterns(withPluralS(config.wordlists().block()));
        this.blockEmbedded = KeywordMatcher.fromPatterns(withPluralS(config.wordlists().blockEmbedded()));
        this.flag = KeywordMatcher.fromPatterns(withPluralS(config.wordlists().flag()));

        cooldownUntilMs.clear();
        buckets.clear();
        lastMessage.clear();

        plugin.getLogger().info("[ChatFilter] loaded: enabled=" + config.enabled()
                + " allow=" + config.wordlists().allow().size()
                + " block=" + config.wordlists().block().size()
                + " flag=" + config.wordlists().flag().size());
    }

    public ChatFilterConfig config() {
        return config;
    }

    public Decision check(Player player, String text, Channel channel) {
        if (!config.enabled()) return Decision.allow(null, channel);
        if (player != null && player.hasPermission(config.permissions().bypassPerm())) return Decision.allow(null, channel);
        if (text == null || text.isBlank()) return Decision.allow(null, channel);

        long now = System.currentTimeMillis();
        NormalizedMessage nm = normalizer.normalize(text, config.normalization());

        if (!UnicodePolicyChecker.isAllowed(nm.visible(), config.unicodePolicy())) {
            Decision d = new Decision(ChatFilterConfig.Action.BLOCK, "unicode.policy", "Disallowed characters", null, "visible", nm, channel);
            applyCooldown(player, now);
            return d;
        }

        if (player != null) {
            Long until = cooldownUntilMs.get(player.getUniqueId());
            if (until != null && now < until) {
                return new Decision(ChatFilterConfig.Action.BLOCK, "cooldown", "Filtered message cooldown", null, null, nm, channel);
            }
        }

        var heur = runHeuristics(player, nm, now, channel);
        if (heur != null && heur.action() != ChatFilterConfig.Action.ALLOW) {
            if (heur.blocked()) applyCooldown(player, now);
            return heur;
        }

        boolean allowHit = matchAcrossViews(allow, nm, config.views().wordlists().allow(), false).isPresent();
        if (!allowHit) {
            var embeddedHit = matchAcrossViews(blockEmbedded, nm, config.views().wordlists().blockEmbedded(), true);
            if (embeddedHit.isPresent()) {
                var h = embeddedHit.get();
                Decision d = new Decision(ChatFilterConfig.Action.BLOCK, "wordlist.block_embedded", "Blocked word (embedded)", h.pattern(), h.view(), nm, channel);
                applyCooldown(player, now);
                return d;
            }

            var blockHit = matchAcrossViews(block, nm, config.views().wordlists().block(), false);
            if (blockHit.isPresent()) {
                var h = blockHit.get();
                Decision d = new Decision(ChatFilterConfig.Action.BLOCK, "wordlist.block", "Blocked word/phrase", h.pattern(), h.view(), nm, channel);
                applyCooldown(player, now);
                return d;
            }
        }

        var flagHit = matchAcrossViews(flag, nm, config.views().wordlists().flag(), false);
        if (flagHit.isPresent()) {
            var h = flagHit.get();
            return new Decision(ChatFilterConfig.Action.FLAG, "wordlist.flag", "Flagged word/phrase", h.pattern(), h.view(), nm, channel);
        }

        return Decision.allow(nm, channel);
    }

    private Optional<KeywordMatcher.Hit> matchAcrossViews(
            KeywordMatcher matcher,
            NormalizedMessage nm,
            List<String> viewNames,
            boolean embedded
    ) {
        if (matcher == null || viewNames == null || viewNames.isEmpty()) return Optional.empty();

        for (String rawView : viewNames) {
            NormalizedMessage.View v = NormalizedMessage.parseView(rawView);
            if (v == null) continue;

            String text = nm.view(v);
            if (text == null || text.isBlank()) continue;

            KeywordMatcher.Boundary boundary = boundaryFor(v, embedded);
            var hit = matcher.firstLongest(text, rawView, boundary);
            if (hit.isPresent()) return hit;
        }

        return Optional.empty();
    }

    private static KeywordMatcher.Boundary boundaryFor(NormalizedMessage.View v, boolean embedded) {
        if (embedded) return KeywordMatcher.Boundary.NONE;
        return switch (v) {
            case SPACED -> KeywordMatcher.Boundary.TOKEN_SPACES;
            case COMPACT, VISIBLE -> KeywordMatcher.Boundary.ALPHANUM;
        };
    }

    private Decision runHeuristics(Player player, NormalizedMessage nm, long now, Channel channel) {
        if (!config.heuristics().enabled()) return null;

        var rl = config.heuristics().rateLimit();
        if (rl.enabled() && player != null) {
            TokenBucket b = buckets.computeIfAbsent(player.getUniqueId(), id -> new TokenBucket(rl.capacity(), rl.refillPerSecond(), now));
            if (!b.tryConsume(1.0, now)) {
                return new Decision(rl.action(), "heuristics.rate_limit", "Too many messages (rate limit)", null, null, nm, channel);
            }
        }

        var dup = config.heuristics().duplicate();
        if (dup.enabled() && player != null) {
            long windowMs = Math.max(0, dup.windowSeconds()) * 1000L;
            LastMessage last = lastMessage.get(player.getUniqueId());
            if (last != null && windowMs > 0 && (now - last.atMs) <= windowMs && Objects.equals(last.compact, nm.compact())) {
                return new Decision(dup.action(), "heuristics.duplicate", "Duplicate/near-duplicate spam", null, "compact", nm, channel);
            }
            lastMessage.put(player.getUniqueId(), new LastMessage(nm.compact(), now));
        }

        var links = config.heuristics().links();
        if (links.enabled()) {
            String hit = LinkHeuristics.detect(nm, links);
            if (hit != null) {
                return new Decision(links.action(), "heuristics.links", "Links/invites are not allowed", hit, "visible", nm, channel);
            }
        }

        return null;
    }

    private void applyCooldown(Player player, long now) {
        if (player == null) return;
        int s = Math.max(0, config.enforcement().cooldownSecondsOnViolation());
        if (s <= 0) return;
        cooldownUntilMs.put(player.getUniqueId(), now + s * 1000L);
    }

    public void notifyStaff(Player player, String message, Decision decision) {
        if (player == null || decision == null) return;
        String perm = config.permissions().notifyPerm();
        if (perm == null || perm.isBlank()) return;

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (!staff.hasPermission(perm)) continue;

            staff.sendMessage(TextUtils.deserializeString(
                    "<red>[ChatFilter]</red> <gray><player></gray> <yellow><action></yellow> <gray>(<rule>)</gray>: <white><msg></white>",
                    TagResolver.resolver(
                            Placeholder.parsed("player", player.getName()),
                            Placeholder.parsed("action", decision.action().name()),
                            Placeholder.parsed("rule", String.valueOf(decision.ruleId())),
                            Placeholder.parsed("msg", message)
                    )
            ));
        }

        if (decision.blocked() && config.observability().logViolations()) {
            plugin.getLogger().info("[ChatFilter] BLOCK player=" + player.getName()
                    + " rule=" + decision.ruleId()
                    + " matched=" + decision.matched()
                    + " view=" + decision.view()
                    + " msg=" + message);
        } else if (decision.flagged() && config.observability().logFlags()) {
            plugin.getLogger().info("[ChatFilter] FLAG player=" + player.getName()
                    + " rule=" + decision.ruleId()
                    + " matched=" + decision.matched()
                    + " view=" + decision.view()
                    + " msg=" + message);
        }
    }

    public void notifyPlayerBlocked(Player player) {
        if (player == null) return;
        if (!config.enforcement().notifyPlayerOnBlock()) return;

        String msg = config.enforcement().notifyPlayerMessage();
        if (msg == null || msg.isBlank()) return;

        player.sendMessage(TextUtils.deserializeString(msg));
    }

    private static List<String> withPluralS(List<String> base) {
        if (base == null || base.isEmpty()) return List.of();

        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String s : base) {
            if (s == null) continue;
            String t = s.trim();
            if (t.isBlank()) continue;

            out.add(t);
            out.add(t + "s");
        }
        return new ArrayList<>(out);
    }
}