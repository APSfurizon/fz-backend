package net.furizon.backend.utils;

import java.util.Arrays;

public class TextUtil {

    public static String leadingSlash (String input) {
        return Character.valueOf(input.charAt(input.length() - 1)).equals('/') ? input : input + "/";
    }

    public static String url(String ...strings) {
        return String.join("/", Arrays.asList(strings));
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

}
