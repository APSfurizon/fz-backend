package net.furizon.backend.infrastructure.pretix.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum OrderStatus {
    CANCELED("c"),
    PENDING("n"),
    PAID("p"),
    EXPIRED("e");

    @Getter
    private final String code;

    private static final Map<String, OrderStatus> MAP = Arrays
        .stream(OrderStatus.values())
        .collect(Collectors.toMap(OrderStatus::getCode, Function.identity()));

    public static OrderStatus get(String code) {
        return MAP.get(code);
    }
}
