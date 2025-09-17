package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.dto.requests.EmailRequest;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.email.model.TemplateMessage;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static net.furizon.backend.feature.authentication.AuthenticationEmailTexts.TEMPLATE_PW_RESET;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResetPasswordUseCase implements UseCase<EmailRequest, Boolean> {
    @NotNull
    private final SessionAuthenticationManager sessionAuthenticationManager;

    @NotNull
    private final FrontendConfig frontendConfig;

    @NotNull
    private final EmailSender sender;

    @NotNull
    private final TranslationService translationService;

    @Override
    public @NotNull Boolean executor(@NotNull EmailRequest input) {
        String email = input.getEmail();
        log.info("Initializing a password reset for email {}", email);

        UUID resetPwId = sessionAuthenticationManager.initResetPassword(email);
        if (resetPwId == null) {
            //If an error occurs, return gracefully to not leak emails
            return true;
        }
        log.info("Password reset for email {} created", email);

        String url = frontendConfig.getPasswordResetUrl(resetPwId);

        sender.fireAndForget(
            new MailRequest()
                .to(email)
                .subject(translationService.email("mail.password_reset.title"))
                .templateMessage(
                    TemplateMessage.of(TEMPLATE_PW_RESET)
                    .addParam("link", url)
                )
        );

        return true;
    }
}
