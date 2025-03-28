package net.furizon.backend.infrastructure.pretix.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum QuestionType {
    NUMBER("N"),
    STRING_ONE_LINE("S"),
    STRING_MULTI_LINE("T"),
    BOOLEAN("B"),
    LIST_SINGLE_CHOICE("C"),
    LIST_MULTIPLE_CHOICE("M"),
    FILE("F"),
    DATE("D"),
    TIME("H"),
    DATE_TIME("W"),
    COUNTRY_CODE("CC"),
    PHONE_NUMBER("TEL");

    @JsonValue
    @Getter
    private final String code;

    private static final Map<String, QuestionType> MAP = Arrays
        .stream(QuestionType.values())
        .collect(Collectors.toMap(QuestionType::getCode, Function.identity()));

    public static QuestionType get(@NotNull String code) {
        return MAP.get(code);
    }
}
