package net.furizon.backend.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import net.furizon.backend.infrastructure.web.dto.ApiError;
import net.furizon.backend.infrastructure.web.dto.HttpErrorResponse;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        //.forEach(
        //(error) -> {
        //if (error instanceof FieldError fieldError) {
        //String fieldName = fieldError.getField();
        //String errorMessage = fieldError.getDefaultMessage();
        //errors.put(fieldName, errorMessage);
        //} else {
        //generalErrors.add(error.getDefaultMessage());
        //}
        //}
        //);
        final var errors = ex
            .getBindingResult()
            .getAllErrors()
            .stream()
            .map(
                (err) ->
                    new ApiError(
                        err.getDefaultMessage(),
                        ""
                    )
            )
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
}
