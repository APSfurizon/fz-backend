package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.dto.requests.ChangeMailRequest;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.EmailVars;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static net.furizon.backend.feature.authentication.AuthenticationEmailTexts.TEMPLATE_EMAIL_CHANGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeEmailUseCase implements UseCase<ChangeEmailUseCase.Input, Boolean> {

    @NotNull private final SessionAuthenticationManager sessionAuthenticationManager;
    @NotNull private final UserFinder userFinder;
    @NotNull private final EmailSender sender;
    @NotNull private final GeneralChecks generalChecks;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        //KEEP IN MIND! We currently ONLY support changing email as an admin!!!
        if (input.req.getUserId() == null) {
            throw new ApiException("Only admin email change is currently supported");
        }
        long userId = generalChecks.getUserIdAndAssertPermission(
                input.req.getUserId(),
                input.user,
                Permission.CAN_MANAGE_USER_LOGIN
        );

        //No race condition here, since there ALWAYS be a correct email first being sent
        UserEmailData prevMailData = Objects.requireNonNull(userFinder.getMailDataForUser(userId));
        String newMail = input.req.getEmail();
        UserEmailData newMailData = new UserEmailData(
                userId,
                newMail,
                prevMailData.getFursonaName(),
                prevMailData.getLanguage()
        );

        sessionAuthenticationManager.changeEmail(userId, newMail);

        MailVarPair var = MailVarPair.of(EmailVars.EMAIL, newMail);
        sender.fireAndForgetMany(
            new MailRequest(prevMailData, TEMPLATE_EMAIL_CHANGE, var).subject("mail.email_change.title"),
            new MailRequest(newMailData, TEMPLATE_EMAIL_CHANGE, var).subject("mail.email_change.title")
        );

        return true;
    }

    public record Input(
        @NotNull FurizonUser user,
        @NotNull ChangeMailRequest req
    ) {}
}
