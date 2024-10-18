package net.furizon.backend.infrastructure.web.exception;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@ToString
public class ApiException extends RuntimeException {
    @NotNull
    private final HttpStatus status;

    @NotNull
    private final String message;

    // TODO -> Do we need it here?
    @Nullable
    private List<Object> errors;

    public ApiException(@NotNull String message) {
        this.status = HttpStatus.BAD_REQUEST;
        this.message = message;
    }

    public ApiException(
        @NotNull HttpStatus status,
        @NotNull String message
    ) {
        this.status = status;
        this.message = message;
    }
}
