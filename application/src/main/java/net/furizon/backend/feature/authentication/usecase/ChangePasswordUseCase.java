package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                //TODO error fix
                //throw new ApiException("ResetPwId not found", AuthenticationCodes.PW_RESET_NOT_FOUND);
            }
        }

        log.info("Changing password for user {}", userId);
        sessionAuthenticationManager.changePassword(userId, input.req.getNewPassword());

        //TODO EMAIL notify user that his account has changed password

        return true;
    }

    public record Input(
            @Nullable FurizonUser user,
            @NotNull ChangePasswordRequest req
    ) {}
}
