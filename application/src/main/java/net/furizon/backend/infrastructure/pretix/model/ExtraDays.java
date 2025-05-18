package net.furizon.backend.infrastructure.pretix.model;

public enum ExtraDays {
    NONE,
    EARLY,
    LATE,
    BOTH;

    public static final long EARLY_DAYS_NO = 1L;
    public static final long LATE_DAYS_NO = 1L;

    public boolean isEarly() {
        return (this.ordinal() & 0b01) != 0;
    }

    public boolean isLate() {
        return (this.ordinal() & 0b10) != 0;
    }

    public static ExtraDays get(int ordinal) {
        return ExtraDays.values()[ordinal];
    }

    public static ExtraDays or(ExtraDays saved, ExtraDays in) {
        if (saved != ExtraDays.BOTH) {
            if (saved != in && saved != ExtraDays.NONE) {
                saved = ExtraDays.BOTH;
            } else {
                saved = in;
            }
        }
        return saved;
    }
}
