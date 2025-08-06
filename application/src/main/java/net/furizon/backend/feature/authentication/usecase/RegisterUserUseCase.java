package net.furizon.backend.feature.authentication.usecase;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.authentication.AuthenticationCodes;
import net.furizon.backend.feature.authentication.dto.requests.RegisterUserRequest;
import net.furizon.backend.feature.membership.validation.PersonalUserInformationValidator;
import net.furizon.backend.feature.membership.action.addMembershipInfo.AddMembershipInfoAction;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.action.createUser.CreateUserAction;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.EmailVars;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.TEMPLATE_EMAIL_CONFIRM;
import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.SUBJECT_EMAIL_CONFIRM;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterUserUseCase implements UseCase<RegisterUserUseCase.Input, User> {
    @NotNull
    private final PersonalUserInformationValidator validation;

    @NotNull
    private final CreateUserAction createUserAction;

    @NotNull
    private final SessionAuthenticationManager sessionAuthenticationManager;

    @NotNull
    private final AddMembershipInfoAction addMembershipInfoAction;

    @NotNull
    private final FrontendConfig frontendConfig;

    @NotNull
    private final EmailSender sender;

    @NotNull
    private final TranslationService translationService;

    @Transactional
    @Override
    public @NotNull User executor(@NotNull RegisterUserUseCase.Input input) {
        RegisterUserRequest regUserReq = input.user;
        String email = regUserReq.getEmail();
        log.info("Registering account with email {}", email);

        final var authentication = sessionAuthenticationManager.findAuthenticationByEmail(regUserReq.getEmail());
        if (authentication != null) {
            throw new ApiException(
                translationService.error("authentication.email.account_exists", new Object[] {regUserReq.getEmail()}),
                AuthenticationCodes.EMAIL_ALREADY_REGISTERED
            );
        }

        validation.validate(regUserReq.getPersonalUserInformation());
        User user = createUserAction.invoke(
                regUserReq.getFursonaName(),
                regUserReq.getPersonalUserInformation().getResidenceCountry()
        );
        UUID confirmationId = sessionAuthenticationManager.createAuthentication(
            user.getId(),
            email,
            regUserReq.getPassword()
        );
        addMembershipInfoAction.invoke(
            user.getId(),
            regUserReq.getPersonalUserInformation(),
            input.event
        );

        log.debug("Account registration of {}: Sent confirmation uuid {}", email, confirmationId);

        //TODO automatically generate the confirmation url from the current request and the
        // path to the /confirm-url endpoint
        //HttpServletRequest request = input.request;
        //ServletUriComponentsBuilder.fromServletMapping(request).build();

        sender.fireAndForget(
            new MailRequest()
                .to(email)
                .subject(SUBJECT_EMAIL_CONFIRM)
                .templateMessage(
                    TEMPLATE_EMAIL_CONFIRM, null,
                    MailVarPair.of(EmailVars.LINK, frontendConfig.getConfirmEmailUrl(confirmationId))
                )
        );

        return user;
    }

    public record Input(
        @NotNull HttpServletRequest request,
        @NotNull RegisterUserRequest user,
        @NotNull Event event
    ) {}
}
