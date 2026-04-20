package net.furizon.backend.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.web.dto.ApiError;
import net.furizon.backend.infrastructure.web.dto.HttpErrorResponse;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Stream;

import static net.furizon.backend.infrastructure.web.Web.Constants.Mdc.MDC_CORRELATION_ID;

@Slf4j
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
        log.error("Exception while handling request {}", (String) request.getAttribute(MDC_CORRELATION_ID), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(HttpErrorResponse.builder()
            .errors(List.of(new ApiError("oof :c", GeneralResponseCodes.GENERIC_ERROR)))
            .requestId((String) request.getAttribute(MDC_CORRELATION_ID)).build());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<HttpErrorResponse> handleException(
            @NotNull HttpMediaTypeNotSupportedException ex,
            @NotNull HttpServletRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(HttpErrorResponse.builder()
                    .errors(List.of(new ApiError(translationService.error("common.invalid_mismatch_media_type"),
                            GeneralResponseCodes.GENERIC_ERROR)))
                    .requestId((String) request.getAttribute(MDC_CORRELATION_ID)).build());
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

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<HttpErrorResponse> handleNoResourceFoundException(
            @NotNull NoResourceFoundException ex,
            @NotNull HttpServletRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                HttpErrorResponse.builder()
                        .errors(List.of(
                            new ApiError(
                                translationService.error("common.resource_not_found"),
                                GeneralResponseCodes.GENERIC_ERROR
                            )
                        ))
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

    @ExceptionHandler(TypeMismatchException.class)
    ResponseEntity<HttpErrorResponse> handleTypeMismatchException(
            @NotNull TypeMismatchException ex,
            @NotNull HttpServletRequest request
    ) {
        var error = new ApiError(
                translationService.error(
                        "common.invalid_request_input_type_mismatch",
                        ex.getPropertyName(),
                        ex.getRequiredType() == null ? "-" : ex.getRequiredType().getName(),
                        ex.getValue()
                ),
                ApiCommonErrorCode.INVALID_INPUT
        );
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(
                HttpErrorResponse.builder()
                        .errors(List.of(error))
                        .requestId((String) request.getAttribute(MDC_CORRELATION_ID))
                    .build()
            );
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<HttpErrorResponse> handleMissingServletRequestParameterException(
            @NotNull MissingServletRequestParameterException ex,
            @NotNull HttpServletRequest request
    ) {
        var error = new ApiError(
                translationService.error(
                    "common.invalid_request_input_missing_param",
                    ex.getParameterName(),
                    ex.getParameterType()
                ),
                ApiCommonErrorCode.INVALID_INPUT
        );
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(
                HttpErrorResponse.builder()
                        .errors(List.of(error))
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
                .flatMap(this::matchParameterResult)
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

    private Stream<ApiError> matchParameterResult(@NotNull ParameterValidationResult result) {
        MethodParameter methodParam = result.getMethodParameter();
        Object rejectedValue = result.getArgument();

        // Extract custom name (fallback to parameter name)
        String paramName = methodParam.getParameterName();
        RequestParam requestParamAnno = methodParam.getParameterAnnotation(RequestParam.class);
        if (requestParamAnno != null && !requestParamAnno.name().isEmpty()) {
            paramName = requestParamAnno.name();
        }

        // Extract regex
        Pattern patternAnno = methodParam.getParameterAnnotation(Pattern.class);
        String regex = (patternAnno != null) ? patternAnno.regexp() : "No regex specified";
        final String finalParamName = paramName; // For use in lambda

        return result.getResolvableErrors().stream().map(error -> {
            // If it's a FieldError or ObjectError, let our shared helper handle it!
            if (error instanceof FieldError || error instanceof ObjectError) {
                return matchObjectError(error);
            }

            // Otherwise, it's a scalar parameter error. Handle it using the extracted context.
            return new ApiError(
                    translationService.error(
                            "common.invalid_request_input_regex",
                            finalParamName,
                            rejectedValue,
                            regex
                    ),
                    ApiCommonErrorCode.INVALID_INPUT
            );
        });
    }

    @NotNull
    private ApiError matchObjectError(@NotNull MessageSourceResolvable error) {
        log.debug("matchObjectError class = {}", error.getClass());
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
