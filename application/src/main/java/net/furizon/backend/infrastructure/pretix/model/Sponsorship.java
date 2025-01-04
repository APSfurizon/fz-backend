package net.furizon.backend.infrastructure.pretix.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Sponsorship {
    NONE(""),
    SPONSOR(""),
    SUPER_SPONSOR(""); //These MUST be in "importance" order

    private final String color; //TODO for frontend

    private static final Map<Integer, Sponsorship> MAP = Arrays
        .stream(Sponsorship.values())
        .collect(Collectors.toMap(Sponsorship::ordinal, Function.identity()));

    public static Sponsorship get(int ordinal) {
        return MAP.get(ordinal);
    }
}
