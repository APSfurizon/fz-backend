package net.furizon.backend.utils;

public class TextUtil {
    public static String leadingSlash(String input) {
        return Character.valueOf(input.charAt(input.length() - 1)).equals('/') ? input : input + "/";
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
