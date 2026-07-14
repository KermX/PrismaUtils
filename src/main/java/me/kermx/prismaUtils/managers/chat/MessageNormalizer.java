package me.kermx.prismaUtils.managers.chat;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

public final class MessageNormalizer {

    private static final Map<Integer, String> CONFUSABLES = Map.ofEntries(
            Map.entry((int) 'а', "a"),
            Map.entry((int) 'е', "e"),
            Map.entry((int) 'о', "o"),
            Map.entry((int) 'р', "p"),
            Map.entry((int) 'с', "c"),
            Map.entry((int) 'у', "y"),
            Map.entry((int) 'х', "x"),
            Map.entry((int) 'і', "i")
    );

    public NormalizedMessage normalize(String original, ChatFilterConfig.Normalization cfg) {
        if (original == null) original = "";

        String s = original;

        if (cfg.nfkcCaseFold()) {
            s = Normalizer.normalize(s, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        } else {
            s = s.toLowerCase(Locale.ROOT);
        }

        if (cfg.confusableFolding()) {
            s = foldConfusables(s);
        }

        if (cfg.latinSkeletonFolding()) {
            s = foldLatinSkeleton(s);
        }

        if (cfg.removeInvisibles()) {
            s = stripInvisibles(s);
        }

        if (cfg.maxRepeat() > 0) {
            s = collapseRepeats(s, cfg.maxRepeat());
        }

        String visible = s;

        if (!cfg.separatorFolding()) {
            return new NormalizedMessage(original, visible, visible, visible);
        }

        String spaced = toSpaced(visible);
        String compact = toCompact(visible);
        return new NormalizedMessage(original, visible, spaced, compact);
    }

    private static String foldLatinSkeleton(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);

            switch (cp) {
                case '4', '@', 'å', 'ä', 'á', 'à' -> out.append('a');
                case '6' -> out.append('b');
                case '(', '<' -> out.append('c');
                case '3', 'é', 'è' -> out.append('e');
                case '#' -> out.append('h');
                case 'í', '1', '!', '|', '/' -> out.append('i');
                case 'ñ' -> out.append('n');
                case '0', 'ö', 'ó' -> out.append('o');
                case '5', '$' -> out.append('s');
                case '7', '+' -> out.append('t');
                case 'ú', 'ü' -> out.append('u');
                case '2' -> out.append('z');
                default -> out.appendCodePoint(cp);
            }
        }
        return out.toString();
    }

    private static String stripInvisibles(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);

            if (Character.getType(cp) == Character.FORMAT) continue;
            if (cp == 0x200B || cp == 0x200C || cp == 0x200D || cp == 0xFEFF) continue;

            out.appendCodePoint(cp);
        }
        return out.toString();
    }

    private static String collapseRepeats(String s, int max) {
        StringBuilder out = new StringBuilder(s.length());
        int last = -1;
        int run = 0;

        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);

            if (cp == last) run++;
            else {
                last = cp;
                run = 1;
            }

            if (run <= max) out.appendCodePoint(cp);
        }
        return out.toString();
    }

    private static boolean isSeparatorish(int cp) {
        if (Character.isWhitespace(cp)) return true;
        int t = Character.getType(cp);
        return t == Character.CONNECTOR_PUNCTUATION
                || t == Character.DASH_PUNCTUATION
                || t == Character.START_PUNCTUATION
                || t == Character.END_PUNCTUATION
                || t == Character.OTHER_PUNCTUATION
                || t == Character.MATH_SYMBOL
                || t == Character.CURRENCY_SYMBOL
                || t == Character.MODIFIER_SYMBOL
                || t == Character.OTHER_SYMBOL;
    }

    private static String toSpaced(String s) {
        StringBuilder out = new StringBuilder(s.length());
        boolean inSep = true;

        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);

            if (isSeparatorish(cp)) {
                if (!inSep) {
                    out.append(' ');
                    inSep = true;
                }
                continue;
            }

            out.appendCodePoint(cp);
            inSep = false;
        }

        return out.toString().trim().replaceAll("\\s+", " ");
    }

    private static String toCompact(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);

            if (isSeparatorish(cp)) continue;
            out.appendCodePoint(cp);
        }
        return out.toString();
    }

    private static String foldConfusables(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);

            String mapped = CONFUSABLES.get(cp);
            if (mapped != null) out.append(mapped);
            else out.appendCodePoint(cp);
        }
        return out.toString();
    }
}