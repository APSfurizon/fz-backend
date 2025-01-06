package net.furizon.backend.feature.authentication.usecase;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.dto.requests.RegisterUserRequest;
import net.furizon.backend.feature.authentication.validation.RegisterUserValidation;
import net.furizon.backend.feature.membership.action.addMembershipInfo.AddMembershipInfoAction;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.action.createUser.CreateUserAction;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.email.model.TemplateMessage;
import net.furizon.backend.infrastructure.security.session.manager.SessionAuthenticationManager;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.SUBJECT_PW_RESET;
import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.TEMPLATE_PW_RESET;

@Component
@RequiredArgsConstructor
public class RegisterUserUseCase implements UseCase<RegisterUserUseCase.Input, User> {

    @NotNull private final RegisterUserValidation validation;

    @NotNull private final CreateUserAction createUserAction;
    @NotNull private final SessionAuthenticationManager sessionAuthenticationManager;
    @NotNull private final AddMembershipInfoAction addMembershipInfoAction;

    @NotNull private final FrontendConfig frontendConfig;
    @NotNull private final EmailSender sender;

    @Transactional
    @Override
    public @NotNull User executor(@NotNull RegisterUserUseCase.Input input) {
        RegisterUserRequest regUserReq = input.user;
        String email = regUserReq.getEmail();

        validation.validate(regUserReq);
        final var user = createUserAction.invoke(regUserReq.getFursonaName());
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

        //TODO automatically generate the confirmation url from the current request and the
        // path to the /confirm-url endpoint
        //HttpServletRequest request = input.request;
        //ServletUriComponentsBuilder.fromServletMapping(request).build();

        sender.fireAndForget(
            MailRequest.builder()
                .to(email)
                .subject(SUBJECT_PW_RESET)
                .templateMessage(
                    TemplateMessage.of(TEMPLATE_PW_RESET)
                    .addParam("link", frontendConfig.getConfirmEmailUrl(confirmationId))
                )
            .build()
        );

        return user;
    }

    public record Input(
            @NotNull HttpServletRequest request,
            @NotNull RegisterUserRequest user,
            @Nullable Event event
    ){}
}
