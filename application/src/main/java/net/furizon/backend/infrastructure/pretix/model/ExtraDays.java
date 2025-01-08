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

    public boolean isEarly() {
        return (this.ordinal() & 0b01) != 0;
    }

    public boolean isLate() {
        return (this.ordinal() & 0b10) != 0;
    }

    public static ExtraDays get(int ordinal) {
        return ExtraDays.values()[ordinal];
    }
}
