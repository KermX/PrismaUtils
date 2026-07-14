package me.kermx.prismaUtils.managers.chat;

import me.kermx.prismaUtils.PrismaUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;

public class CommandChatProcessor implements Listener {

    private final PrismaUtils plugin;
    private final ChatFilterManager chatFilterManager;

    private Set<String> monitoredRoots = Set.of();
    private Set<String> pmTwoArgs = Set.of();
    private Set<String> pmOneArg = Set.of();
    private Set<String> channelRest = Set.of();

    public CommandChatProcessor(PrismaUtils plugin, ChatFilterManager chatFilterManager) {
        this.plugin = plugin;
        this.chatFilterManager = chatFilterManager;
        reloadCommandRoots();
    }

    private void reloadCommandRoots() {
        ChatFilterConfig cfg = chatFilterManager.config();
        ChatFilterConfig.Commands commands = cfg.commands();

        monitoredRoots = toLowerSet(commands.monitoredRoots());
        pmTwoArgs = toLowerSet(commands.roots().pmTwoArgs());
        pmOneArg = toLowerSet(commands.roots().pmOneArg());
        channelRest = toLowerSet(commands.roots().channelRest());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!chatFilterManager.config().commands().enabled()) return;

        Player player = event.getPlayer();
        String fullCommand = event.getMessage();
        if (fullCommand == null || fullCommand.isBlank() || !fullCommand.startsWith("/")) return;

        ParsedCommand cmd = ParsedCommand.parse(fullCommand);
        if (cmd == null) return;

        if (!monitoredRoots.contains(cmd.root())) return;

        Extraction extraction = extract(cmd);
        if (extraction == null || extraction.text().isBlank()) return;

        ChatFilterService.Decision decision = chatFilterManager.checkMessage(player, extraction.text(), extraction.channel());

        if (decision.blocked()) {
            event.setCancelled(true);
        }

        if (decision.blocked() || decision.flagged()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                chatFilterManager.notifyStaff(player, "Command /" + cmd.root() + ": " + extraction.text(), decision);
                if (decision.blocked()) {
                    chatFilterManager.notifyPlayer(player);
                }
            });
        }
    }

    private Extraction extract(ParsedCommand cmd) {
        String root = cmd.root();
        List<String> args = cmd.args();

        if (pmTwoArgs.contains(root)) {
            if (args.size() < 2) return null;
            return new Extraction(joinFrom(args, 1), ChatFilterService.Channel.COMMAND_PRIVATE);
        }

        if (pmOneArg.contains(root)) {
            if (args.isEmpty()) return null;
            return new Extraction(joinFrom(args, 0), ChatFilterService.Channel.COMMAND_PRIVATE);
        }

        if (channelRest.contains(root)) {
            if (cmd.argsRaw().isBlank()) return null;
            return new Extraction(cmd.argsRaw(), ChatFilterService.Channel.COMMAND_CHANNEL);
        }

        if (cmd.argsRaw().isBlank()) return null;
        return new Extraction(cmd.argsRaw(), ChatFilterService.Channel.COMMAND_OTHER);
    }

    private static String joinFrom(List<String> args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.size(); i++) {
            if (i > start) sb.append(' ');
            sb.append(args.get(i));
        }
        return sb.toString();
    }

    private static Set<String> toLowerSet(List<String> list) {
        if (list == null || list.isEmpty()) return Set.of();
        Set<String> out = new HashSet<>();
        for (String s : list) {
            if (s == null) continue;
            String t = s.trim();
            if (!t.isBlank()) out.add(t.toLowerCase(Locale.ROOT));
        }
        return Collections.unmodifiableSet(out);
    }

    private record Extraction(String text, ChatFilterService.Channel channel) {}

    private record ParsedCommand(String root, List<String> args, String argsRaw) {
        static ParsedCommand parse(String full) {
            String s = full.trim();
            if (!s.startsWith("/")) return null;

            s = s.substring(1).trim();
            if (s.isBlank()) return null;

            String[] parts = s.split("\\s+");
            String root = parts[0].toLowerCase(Locale.ROOT);

            List<String> args = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) args.add(parts[i]);

            String argsRaw = (parts.length <= 1) ? "" : s.substring(parts[0].length()).trim();
            return new ParsedCommand(root, Collections.unmodifiableList(args), argsRaw);
        }
    }
}