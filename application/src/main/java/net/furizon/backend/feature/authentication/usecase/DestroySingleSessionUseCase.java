package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.Authentication;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.authentication.dto.requests.DestroySessionRequest;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.security.session.Session;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestroySingleSessionUseCase implements UseCase<DestroySingleSessionUseCase.Input, Boolean> {
    @NotNull private final SessionAuthenticationManager sessionAuthenticationManager;
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        UUID sessionId = UUID.fromString(input.dsr.getSessionId());
        Triple<Session, Authentication, Locale> sessionData = sessionAuthenticationManager
                .findSessionWithAuthenticationById(sessionId);
        if (sessionData != null
            && sessionData.getMiddle().getUserId() != input.user.getUserId()
            && !permissionFinder.userHasPermission(input.user.getUserId(), Permission.CAN_BAN_USERS)
        ) {
            throw new ApiException(
                translationService.error("authentication.sessions.not_your_session"),
                AuthenticationCodes.SESSION_NOT_YOURS
            );
        }
        log.info("Destroying session {} of user {}", input.dsr.getSessionId(), input.user.getUserId());
        return sessionAuthenticationManager.deleteSession(sessionId);
    }

    public record Input(
        @NotNull FurizonUser user,
        @NotNull DestroySessionRequest dsr
    ) {}
}
