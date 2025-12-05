package net.furizon.backend.feature.authentication.validation;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.Authentication;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.authentication.usecase.LoginUserUseCase;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static net.furizon.backend.feature.authentication.AuthenticationEmailTexts.TEMPLATE_TOO_MANY_LOGIN_ATTEMPTS;

@Component
@RequiredArgsConstructor
public class CreateLoginSessionValidation {
    @NotNull private final SessionAuthenticationManager sessionAuthenticationManager;
    @NotNull private final PasswordEncoder passwordEncoder;
    @NotNull private final EmailSender emailSender;
    @NotNull private final UserFinder userFinder;
    @NotNull private final SecurityConfig securityConfig;
    @NotNull private final TranslationService translationService;

    public long validateAndGetUserId(@NotNull LoginUserUseCase.Input input) {
        String email = input.email();
        final Authentication authentication = sessionAuthenticationManager.findAuthenticationByEmail(email);
        if (authentication == null) {
            //Using the same exception to not leak registered emails
            throw createInvalidCredentialsException();
        }

        if (authentication.isDisabled()) {
            throw createdAccountDisabledException();
        }

        if (authentication.getMailVerificationCreationMs() != null) {
            throw new ApiException(
                translationService.error("authentication.email.pending_confirmation"),
                AuthenticationCodes.CONFIRMATION_STILL_PENDING
            );
        }

        final boolean passwordMatches = passwordEncoder.matches(
            securityConfig.getPasswordSalt() + input.password(),
            authentication.getHashedPassword()
        );
        if (!passwordMatches) {
            //Possible botnet bruteforce with race conditions, but cloudflare should protect us :pray:
            if (authentication.getFailedAttempts() > securityConfig.getMaxFailedLoginAttempts()) {
                UserEmailData data = Objects.requireNonNull(userFinder.getMailDataForUser(authentication.getUserId()));
                sessionAuthenticationManager.disableUser(data.getUserId());
                emailSender.fireAndForget(
                    new MailRequest()
                        .to(data.getLanguage(), data.getEmail())
                        .templateMessage(TEMPLATE_TOO_MANY_LOGIN_ATTEMPTS, null)
                        .subject("mail.too_many_login_attempts.title")
                );
                throw createdAccountDisabledException();
            }
            sessionAuthenticationManager.increaseLoginAttempts(email);
            throw createInvalidCredentialsException();
        }

        sessionAuthenticationManager.resetLoginAttempts(email);
        return authentication.getUserId();
    }

    private ApiException createdAccountDisabledException() {
        return new ApiException(
            translationService.error("authentication.login.authentication_disabled"),
            AuthenticationCodes.AUTHENTICATION_IS_DISABLED
        );
    }

    private ApiException createInvalidCredentialsException() {
        return new ApiException(
            translationService.error("authentication.login.invalid_credentials"),
            AuthenticationCodes.INVALID_CREDENTIALS
        );
    }
}
