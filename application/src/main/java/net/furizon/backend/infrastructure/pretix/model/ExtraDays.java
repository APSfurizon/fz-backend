package net.furizon.backend.infrastructure.pretix.model;

public enum ExtraDays {
    NONE,
    EARLY,
    LATE,
    BOTH;

    public static Sponsorship get(int ordinal) {
        return Sponsorship.values()[ordinal];
    }
}
