package net.furizon.backend.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import net.furizon.backend.infrastructure.web.dto.HttpErrorResponse;
import net.furizon.backend.infrastructure.web.dto.MethodArgumentNotValidError;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonControllerAdvice {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<HttpErrorResponse<?>> handleApiException(
        @NotNull ApiException ex,
        @NotNull HttpServletRequest request
    ) {
        return ResponseEntity
            .status(ex.getStatus())
            .body(
                HttpErrorResponse.builder()
                    .message(ex.getMessage())
                    .errors(ex.getErrors())
                    .requestId(request.getRequestId()) // TODO -> Implement it later
                    .build()
            );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<HttpErrorResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
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
                (err) -> // TODO -> error instanceof FieldError fieldError
                    MethodArgumentNotValidError.builder()
                        .message(err.getDefaultMessage())
                        .build()
            )
            .toList();
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(
                HttpErrorResponse.<MethodArgumentNotValidError>builder()
                    .message("Unprocessable entity")
                    .errors(errors)
                    .requestId("requestId")
                    .build()
            );
    }
}
