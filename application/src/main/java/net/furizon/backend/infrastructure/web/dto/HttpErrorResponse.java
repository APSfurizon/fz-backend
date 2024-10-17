package net.furizon.backend.infrastructure.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Builder
public class HttpErrorResponse<R> {
    @NotNull
    private final String message;

    @Nullable
    private final List<R> errors;

    @NotNull
    private final String requestId;
}
