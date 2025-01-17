package net.furizon.backend.infrastructure.media;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum StoreMethod {
    DISK(0);

    @Getter
    private final int methodId;

    private static final Map<Integer, StoreMethod> PERMISSIONS = Arrays
            .stream(StoreMethod.values())
            .collect(Collectors.toMap(StoreMethod::getMethodId, Function.identity()));

    @Nullable
    public static StoreMethod get(final int value) {
        return PERMISSIONS.get(value);
    }
}
