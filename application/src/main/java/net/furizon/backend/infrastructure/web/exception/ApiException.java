package net.furizon.backend.infrastructure.web.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Builder
@ToString
public class ApiException extends RuntimeException {
    @NotNull
    @Builder.Default
    private final HttpStatus status = HttpStatus.BAD_REQUEST;

    @NotNull
    private final String message;

    // TODO -> Do we need it here?
    @Nullable
    private List<Object> errors;
}
