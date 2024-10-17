package net.furizon.backend.infrastructure.pretix.dto;

import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

@Data
public class PretixPaging<R> {
    private final int page;

    @Nullable
    private final String next;

    @Nullable
    private final String prev;

    // Can't find any info if is nullable
    @Nullable
    private final List<R> results;

    public static <R> ParameterizedTypeReference<PretixPaging<R>> parameterizedType() {
        return new ParameterizedTypeReference<>() {
        };
    }

    public static <R> PretixPaging<R> empty() {
        return new PretixPaging<>(
            0,
            null,
            null,
            null
        );
    }
}
