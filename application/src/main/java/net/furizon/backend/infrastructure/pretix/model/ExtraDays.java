package net.furizon.backend.infrastructure.pretix.model;

public enum ExtraDays {
    NONE,
    EARLY,
    LATE,
    BOTH;

    public static ExtraDays get(int ordinal) {
        return ExtraDays.values()[ordinal];
    }
}
