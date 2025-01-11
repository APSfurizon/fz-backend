package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.authentication.dto.requests.ChangePasswordRequest;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.SUBJECT_PW_CHANGED;
import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.TEMPLATE_PW_CHANGED;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangePasswordUseCase implements UseCase<ChangePasswordUseCase.Input, Boolean> {

    @NotNull private final SessionAuthenticationManager sessionAuthenticationManager;
    @NotNull private final EmailSender sender;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        Long userId = input.user == null ? null : input.user.getUserId();
        UUID resetPwId = input.req.getResetPwId();

        if (userId == null) {
            if (resetPwId == null) {
                throw new ApiException("User is not logged in and resetPwId is not specified");
            }

            userId = sessionAuthenticationManager.getUserIdFromPasswordResetReqId(resetPwId);
            if (userId == null) {
                throw new ApiException("ResetPwId not found", AuthenticationCodes.PW_RESET_NOT_FOUND);
            }
        }

        log.info("Changing password for user {}", userId);
        sessionAuthenticationManager.changePassword(userId, input.req.getPassword());
        // Request cleanup
        if (resetPwId != null) {
            sessionAuthenticationManager.deletePasswordResetAttempt(resetPwId);
        }
        sender.send(userId, SUBJECT_PW_CHANGED, TEMPLATE_PW_CHANGED);

        return true;
    }

    public record Input(
            @Nullable FurizonUser user,
            @NotNull ChangePasswordRequest req
    ) {}
}
