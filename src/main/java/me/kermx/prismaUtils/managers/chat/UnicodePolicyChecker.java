package me.kermx.prismaUtils.managers.chat;

import java.util.HashSet;
import java.util.Set;

public final class UnicodePolicyChecker {

    private UnicodePolicyChecker() {}

    public static boolean isAllowed(String s, ChatFilterConfig.UnicodePolicy policy) {
        if (policy == null || policy.mode() == ChatFilterConfig.UnicodeMode.OFF) return true;
        if (s == null || s.isBlank()) return true;

        Set<Integer> extra = toCodePointSet(policy.allowedExtraChars());

        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);

            if (cp <= 0x7F) continue;
            if (extra.contains(cp)) continue;

            if (policy.mode() == ChatFilterConfig.UnicodeMode.ASCII_STRICT) {
                return false;
            }

            if (policy.mode() == ChatFilterConfig.UnicodeMode.SCRIPT_WHITELIST) {
                Character.UnicodeScript sc = Character.UnicodeScript.of(cp);

                if (sc == Character.UnicodeScript.LATIN) continue;
                if (sc == Character.UnicodeScript.COMMON || sc == Character.UnicodeScript.INHERITED) continue;

                return false;
            }
        }

        return true;
    }

    private static Set<Integer> toCodePointSet(String s) {
        Set<Integer> out = new HashSet<>();
        if (s == null || s.isEmpty()) return out;
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            out.add(cp);
        }
        return out;
    }
}
