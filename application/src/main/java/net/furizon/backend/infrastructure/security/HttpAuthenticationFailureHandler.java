package net.furizon.backend.infrastructure.security;

import com.google.common.base.Charsets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.jackson.JsonSerializer;
import net.furizon.backend.infrastructure.security.session.exception.SessionExpiredException;
import net.furizon.backend.infrastructure.web.ApiCommonErrorCode;
import net.furizon.backend.infrastructure.web.dto.ApiError;
import net.furizon.backend.infrastructure.web.dto.HttpErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

import static net.furizon.backend.infrastructure.web.Web.Constants.Mdc.MDC_CORRELATION_ID;

@Component
@RequiredArgsConstructor
public class HttpAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final JsonSerializer jsonSerializer;

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(Charsets.UTF_8.name());
        final var builder = HttpErrorResponse
            .builder()
            .requestId((String) request.getAttribute(MDC_CORRELATION_ID));

        final var error = switch (exception) {
            case SessionExpiredException ex -> builder.errors(
                    Collections.singletonList(
                        new ApiError(
                            ex.getMessage(),
                            ApiCommonErrorCode.SESSION_NOT_FOUND.name()
                        )
                    )
                )
                .build();
            default -> builder.errors(
                    Collections.singletonList(
                        new ApiError(
                            "Authentication is required",
                            ApiCommonErrorCode.UNAUTHENTICATED.name()
                        )
                    )
                )
                .build();
        };

        response
            .getOutputStream()
            .println(
                jsonSerializer.serializeAsString(error)
            );
    }
}
