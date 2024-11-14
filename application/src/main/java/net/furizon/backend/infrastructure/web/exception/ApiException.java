package net.furizon.backend.infrastructure.web.exception;

import lombok.Getter;
import lombok.ToString;
import net.furizon.backend.infrastructure.web.ApiCommonErrorCode;
import net.furizon.backend.infrastructure.web.dto.ApiError;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@ToString
public class ApiException extends RuntimeException {
    @NotNull
    private final HttpStatus status;

    @NotNull
    private final List<ApiError> errors;

    public ApiException(@NotNull String message) {
        this.status = HttpStatus.BAD_REQUEST;
        this.errors = List.of(new ApiError(message, ApiCommonErrorCode.UNKNOWN.name()));
    }

    public ApiException(
        @NotNull String message,
        @NotNull String code
    ) {
        this.status = HttpStatus.BAD_REQUEST;
        this.errors = List.of(new ApiError(message, code));
    }

    public ApiException(
        @NotNull String message,
        ApiError @NotNull ... errors
    ) {
        this.status = HttpStatus.BAD_REQUEST;
        this.errors = List.of(errors);
    }

    public ApiException(
        @NotNull HttpStatus status,
        @NotNull String message,
        @NotNull String code
    ) {
        this.status = status;
        this.errors = List.of(new ApiError(message, code));
    }

    public ApiException(
        @NotNull HttpStatus status,
        @NotNull String message,
        ApiError @NotNull ... errors
    ) {
        this.status = status;
        this.errors = List.of(errors);
    }
}
