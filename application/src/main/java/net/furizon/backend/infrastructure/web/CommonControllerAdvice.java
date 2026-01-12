package net.furizon.backend.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.web.dto.ApiError;
import net.furizon.backend.infrastructure.web.dto.HttpErrorResponse;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

import static net.furizon.backend.infrastructure.web.Web.Constants.Mdc.MDC_CORRELATION_ID;

@RestControllerAdvice
@RequiredArgsConstructor
public class CommonControllerAdvice {

    @NotNull
    private final TranslationService translationService;

    @ExceptionHandler(Exception.class)
    ResponseEntity<HttpErrorResponse> handleException(
            @NotNull Exception ex,
            @NotNull HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.ALREADY_REPORTED)
                .body(HttpErrorResponse.builder()
                .errors(List.of()).requestId((String) request.getAttribute(MDC_CORRELATION_ID)).build());
    }

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

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<HttpErrorResponse> handleAccessDeniedException(
            @NotNull AccessDeniedException ex,
            @NotNull HttpServletRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                HttpErrorResponse.builder()
                    .errors(List.of(
                        new ApiError(
                            translationService.error("common.user_does_not_have_permission"),
                            GeneralResponseCodes.USER_IS_NOT_ADMIN
                        )
                    ))
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
    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<HttpErrorResponse> handleMethodValidationException(
            @NotNull HandlerMethodValidationException ex,
            @NotNull HttpServletRequest request
    ) {
        final var errors = ex
                .getParameterValidationResults()
                .stream()
                .flatMap(result -> result.getResolvableErrors().stream())
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
    private ApiError matchObjectError(@NotNull MessageSourceResolvable error) {
        if (error instanceof FieldError fieldError) {
            return new ApiError(
                translationService.error(
                    "common.invalid_request_input",
                    //error.getDefaultMessage(),
                    fieldError.getField(),
                    fieldError.getRejectedValue()
                ),
                ApiCommonErrorCode.INVALID_INPUT
            );
        }

        //final var message = error.getDefaultMessage();
        return new ApiError(/*message != null ? message :*/ "Unknown error", ApiCommonErrorCode.UNKNOWN);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<HttpErrorResponse> handleRequestNotReadableException(
            @NotNull HttpMessageNotReadableException ex,
            @NotNull HttpServletRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(
                HttpErrorResponse.builder()
                    .errors(List.of(
                        new ApiError(
                            ex.getMessage(),
                            ApiCommonErrorCode.INVALID_INPUT
                        )
                    ))
                    .requestId((String) request.getAttribute(MDC_CORRELATION_ID))
                    .build()
            );
    }
}
