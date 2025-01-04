package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmEmailUseCase implements UseCase<UUID, RedirectView> {
    @NotNull
    private final FrontendConfig frontendConfig;

    @NotNull
    private final SessionAuthenticationManager sessionAuthenticationManager;

    @Override
    public @NotNull RedirectView executor(@NotNull UUID input) {
        log.info("Trying to confirm authentication {}", input);
        boolean success = sessionAuthenticationManager.markEmailAsVerified(input);
        return new RedirectView(
            frontendConfig.getLoginUrl(
                success ? AuthenticationCodes.CONFIRMATION_SUCCESSFULL : AuthenticationCodes.CONFIRMATION_NOT_FOUND
            )
        );
    }
}
