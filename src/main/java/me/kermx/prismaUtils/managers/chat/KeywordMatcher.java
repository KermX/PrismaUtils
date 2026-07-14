package me.kermx.prismaUtils.managers.chat;

import java.util.*;

public final class KeywordMatcher {

    public record Hit(String pattern, String view) {}

    private static final class Node {
        final Map<Character, Node> next = new HashMap<>();
        Node fail;
        final List<String> out = new ArrayList<>();
    }

    private final Node root = new Node();

    public enum Boundary { TOKEN_SPACES, ALPHANUM, NONE }

    public static KeywordMatcher fromPatterns(List<String> patterns) {
        KeywordMatcher m = new KeywordMatcher();
        if (patterns != null) {
            for (String p : patterns) {
                if (p == null) continue;
                String s = p.trim().toLowerCase(Locale.ROOT);
                if (s.isBlank()) continue;
                m.add(s);
            }
        }
        m.build();
        return m;
    }

    public Optional<Hit> firstLongest(String text, String viewName, Boundary boundary) {
        if (text == null || text.isBlank()) return Optional.empty();
        if (boundary == null) boundary = Boundary.NONE;
        return scan(text, viewName, boundary);
    }

    private void add(String s) {
        Node cur = root;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            cur = cur.next.computeIfAbsent(c, k -> new Node());
        }
        cur.out.add(s);
    }

    private void build() {
        ArrayDeque<Node> q = new ArrayDeque<>();
        root.fail = root;

        for (Node child : root.next.values()) {
            child.fail = root;
            q.add(child);
        }

        while (!q.isEmpty()) {
            Node v = q.removeFirst();
            for (var e : v.next.entrySet()) {
                char c = e.getKey();
                Node u = e.getValue();

                Node f = v.fail;
                while (f != root && !f.next.containsKey(c)) f = f.fail;
                u.fail = f.next.getOrDefault(c, root);

                u.out.addAll(u.fail.out);
                q.addLast(u);
            }
        }
    }

    private Optional<Hit> scan(String text, String view, Boundary boundary) {
        Node state = root;
        String best = null;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            while (state != root && !state.next.containsKey(c)) state = state.fail;
            state = state.next.getOrDefault(c, root);

            if (!state.out.isEmpty()) {
                for (String pat : state.out) {
                    int start = i - pat.length() + 1;
                    if (start < 0) continue;

                    if (boundary == Boundary.TOKEN_SPACES && !tokenBoundaryOk(text, start, i)) continue;
                    if (boundary == Boundary.ALPHANUM && !alphaNumBoundaryOk(text, start, i)) continue;

                    if (best == null || pat.length() > best.length()) {
                        best = pat;
                    }
                }
            }
        }

        if (best == null) return Optional.empty();
        return Optional.of(new Hit(best, view));
    }

    private static boolean tokenBoundaryOk(String spaced, int start, int end) {
        boolean left = (start <= 0) || spaced.charAt(start - 1) == ' ';
        boolean right = (end + 1 >= spaced.length()) || spaced.charAt(end + 1) == ' ';
        return left && right;
    }

    private static boolean alphaNumBoundaryOk(String s, int start, int end) {
        boolean leftOk = (start <= 0) || !Character.isLetterOrDigit(s.charAt(start - 1));
        boolean rightOk = (end + 1 >= s.length()) || !Character.isLetterOrDigit(s.charAt(end + 1));
        return leftOk && rightOk;
    }
}
