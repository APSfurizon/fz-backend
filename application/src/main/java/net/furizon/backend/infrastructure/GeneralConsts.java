package net.furizon.backend.infrastructure;

import java.util.regex.Pattern;

public class GeneralConsts {

    public static final String NAME_REGEX = "^[\\p{L}\\p{N}\\p{M}_\\-/!\"'()\\[\\].,&\\\\? ]{2,63}$";
    public static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);
}
