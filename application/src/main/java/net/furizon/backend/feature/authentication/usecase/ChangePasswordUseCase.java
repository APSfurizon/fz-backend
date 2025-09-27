package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.authentication.dto.requests.ChangePasswordRequest;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static net.furizon.backend.feature.authentication.AuthenticationEmailTexts.TEMPLATE_PW_CHANGED;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangePasswordUseCase implements UseCase<ChangePasswordUseCase.Input, Boolean> {

    @NotNull private final SessionAuthenticationManager sessionAuthenticationManager;
    @NotNull private final UserFinder userFinder;
    @NotNull private final EmailSender sender;
    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        Long userId = input.user == null ? null : input.user.getUserId();
        UUID resetPwId = input.req.getResetPwId();

        if (userId == null) {
            if (resetPwId == null) {
                throw new ApiException(
                        translationService.error("authentication.reset.session_and_reset_not_valid"));
            }

            userId = sessionAuthenticationManager.getUserIdFromPasswordResetReqId(resetPwId);
            if (userId == null) {
                throw new ApiException(translationService.error("authentication.reset.reset_not_valid"),
                        AuthenticationCodes.PW_RESET_NOT_FOUND);
            }
        }

        log.info("Changing password for user {}", userId);
        sessionAuthenticationManager.changePassword(userId, input.req.getPassword());
        // Request cleanup
        if (resetPwId != null) {
            sessionAuthenticationManager.deletePasswordResetAttempt(resetPwId);
        }
        sender.fireAndForget(new MailRequest(userId, userFinder, TEMPLATE_PW_CHANGED)
                .subject("mail.password_changed.title"));

        return true;
    }

    public record Input(
            @Nullable FurizonUser user,
            @NotNull ChangePasswordRequest req
    ) {}
}
