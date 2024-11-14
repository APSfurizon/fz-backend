package net.furizon.backend.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import net.furizon.backend.infrastructure.web.dto.ApiError;
import net.furizon.backend.infrastructure.web.dto.HttpErrorResponse;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static net.furizon.backend.infrastructure.web.Web.Constants.Mdc.MDC_CORRELATION_ID;

@RestControllerAdvice
public class CommonControllerAdvice {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<HttpErrorResponse> handleApiException(
        @NotNull ApiException ex,
        @NotNull HttpServletRequest request
    ) {
        return ResponseEntity
            .status(ex.getStatus())
            .body(
                HttpErrorResponse.builder()
                    .errors(ex.getErrors())
                    .requestId((String) request.getAttribute(MDC_CORRELATION_ID))
                    .build()
            );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<HttpErrorResponse> handleMethodArgumentNotValidException(
        @NotNull MethodArgumentNotValidException ex,
        @NotNull HttpServletRequest request
    ) {
        final var errors = ex
            .getBindingResult()
            .getAllErrors()
            .stream()
            .map(this::matchObjectError)
            .toList();
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(
                HttpErrorResponse.builder()
                    .errors(errors)
                    .requestId((String) request.getAttribute(MDC_CORRELATION_ID))
                    .build()
            );
    }

    @NotNull
    private ApiError matchObjectError(@NotNull ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return new ApiError(
                "Field '%s' %s; (value '%s' is invalid)".formatted(
                    fieldError.getField(),
                    error.getDefaultMessage(),
                    fieldError.getRejectedValue()
                ),
                ApiCommonErrorCode.INVALID_INPUT.toString()
            );
        }

        final var message = error.getDefaultMessage();
        return new ApiError(message != null ? message : "Unknown error", ApiCommonErrorCode.UNKNOWN.toString());
    }
}
