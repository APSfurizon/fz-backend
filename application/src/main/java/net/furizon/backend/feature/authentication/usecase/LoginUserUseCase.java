package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.security.session.action.createSession.CreateSessionAction;
import net.furizon.backend.feature.authentication.dto.LoginResponse;
import net.furizon.backend.feature.authentication.validation.CreateLoginSessionValidation;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import net.furizon.backend.infrastructure.security.session.action.clearNewestUserSessions.ClearNewestUserSessionsAction;
import net.furizon.backend.infrastructure.security.session.finder.SessionFinder;
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

    private final CreateSessionAction createSessionAction;

    private final TokenEncoder tokenEncoder;

    private final SessionFinder sessionFinder;

    private final ClearNewestUserSessionsAction clearNewestUserSessionsAction;

    private final SecurityConfig securityConfig;

    @Transactional
    @Override
    public @NotNull LoginResponse executor(@NotNull LoginUserUseCase.Input input) {
        final var userId = validation.validateAndGetUserId(input);
        int sessionsCount = sessionFinder.getUserSessionsCount(userId);
        if (sessionsCount >= securityConfig.getSession().getMaxAllowedSessionsSize()) {
            log.warn(
                "Maximum allowed sessions size reached. Sessions count = '{}', userId = '{}'; Running the cleaning",
                sessionsCount,
                userId
            );
            clearNewestUserSessionsAction.invoke(userId);
        }

        final var sessionId = createSessionAction.invoke(
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
        @NotNull String email,
        @NotNull String password,
        @NotNull String clientIp,
        @Nullable String userAgent
    ) {}
}
