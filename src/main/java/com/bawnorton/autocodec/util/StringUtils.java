package com.bawnorton.autocodec.util;

public final class StringUtils {
    public static String lowerFirst(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }
}
