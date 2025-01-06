package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.dto.responses.LoginResponse;
import net.furizon.backend.feature.authentication.validation.CreateLoginSessionValidation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.security.token.TokenMetadata;
import net.furizon.backend.infrastructure.security.token.encoder.TokenEncoder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginUserUseCase implements UseCase<LoginUserUseCase.Input, LoginResponse> {
    private final CreateLoginSessionValidation validation;

    private final SessionAuthenticationManager sessionAuthenticationManager;

    private final TokenEncoder tokenEncoder;

    private final SecurityConfig securityConfig;

    @Transactional
    @Override
    public @NotNull LoginResponse executor(@NotNull LoginUserUseCase.Input input) {
        if (input.user != null) {
            //TODO error fix
            //throw new ApiException("User is already logged in", AuthenticationCodes.ALREADY_LOGGED_IN);
        }

        final var userId = validation.validateAndGetUserId(input);
        int sessionsCount = sessionAuthenticationManager.getUserSessionsCount(userId);
        if (sessionsCount >= securityConfig.getSession().getMaxAllowedSessionsSize()) {
            log.warn(
                "Maximum allowed sessions size reached. Sessions count = '{}', userId = '{}'; Running the cleaning",
                sessionsCount,
                userId
            );
            sessionAuthenticationManager.clearOldestSessions(userId);
        }

        final var sessionId = sessionAuthenticationManager.createSession(
            userId,
            input.clientIp,
            input.userAgent
        );

        return new LoginResponse(
            userId,
            tokenEncoder.encode(
                TokenMetadata.builder()
                    .userId(userId)
                    .sessionId(sessionId)
                    .build()
            )
        );
    }

    public record Input(
        @Nullable FurizonUser user,
        @NotNull String email,
        @NotNull String password,
        @NotNull String clientIp,
        @Nullable String userAgent
    ) {}
}
