package net.furizon.backend.utils.pretix;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Sponsorship {
    NONE(""),
    SPONSOR(""),
    SUPER_SPONSOR(""); //These MUST be in "importance" order

    private final String color; //TODO for frontend

    public static Sponsorship get(int ordinal) {
        return Sponsorship.values()[ordinal];
    }
}
