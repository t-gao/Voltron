package com.voltron.router.base;

public class StringUtils {
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static String ensureNoneNullString(String s) {
        return s == null ? "" : s;
    }
}
