package net.furizon.backend.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    @NotNull
    private final String requestId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Nullable
    private final List<ApiError> errors;
}
