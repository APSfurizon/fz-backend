package net.furizon.backend.feature.authentication.validation;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.Authentication;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.authentication.usecase.LoginUserUseCase;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.email.model.TemplateMessage;
import net.furizon.backend.infrastructure.security.SecurityConfig;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.SUBJECT_TOO_MANY_LOGIN_ATTEMPTS;
import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.TEMPLATE_TOO_MANY_LOGIN_ATTEMPTS;

@Component
@RequiredArgsConstructor
public class CreateLoginSessionValidation {
    @NotNull
    private final SessionAuthenticationManager sessionAuthenticationManager;

    @NotNull
    private final SecurityConfig securityConfig;

    @NotNull
    private final EmailSender emailSender;

    @NotNull
    private final PasswordEncoder passwordEncoder;

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
                "Email confirmation is still pending",
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
                sessionAuthenticationManager.disableUser(authentication.getUserId());
                emailSender.fireAndForget(
                    new MailRequest()
                        .to(email)
                        .subject(SUBJECT_TOO_MANY_LOGIN_ATTEMPTS)
                        .templateMessage(TEMPLATE_TOO_MANY_LOGIN_ATTEMPTS, null)
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
            "Not possible to login",
            AuthenticationCodes.AUTHENTICATION_IS_DISABLED
        );
    }

    private ApiException createInvalidCredentialsException() {
        return new ApiException(
            "Invalid Credentials",
            AuthenticationCodes.INVALID_CREDENTIALS
        );
    }
}
