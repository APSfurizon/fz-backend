package net.furizon.backend.infrastructure.pretix.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ExtraDays {
    NONE,
    EARLY,
    LATE,
    BOTH;

    private static final Map<Integer, ExtraDays> MAP = Arrays
        .stream(ExtraDays.values())
        .collect(Collectors.toMap(ExtraDays::ordinal, Function.identity()));

    public static ExtraDays get(int ordinal) {
        return MAP.get(ordinal);
    }
}
