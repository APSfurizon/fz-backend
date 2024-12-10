package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.action.createAuthentication.CreateAuthenticationAction;
import net.furizon.backend.feature.authentication.dto.RegisterUserRequest;
import net.furizon.backend.feature.authentication.validation.RegisterUserValidation;
import net.furizon.backend.feature.membership.action.addMembershipInfo.AddMembershipInfoAction;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.action.createUser.CreateUserAction;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class
RegisterUserUseCase implements UseCase<RegisterUserUseCase.Input, User> {
    private final RegisterUserValidation validation;

    private final CreateUserAction createUserAction;
    private final CreateAuthenticationAction createAuthenticationAction;
    private final AddMembershipInfoAction addMembershipInfoAction;
    
    @Transactional
    @Override
    public @NotNull User executor(@NotNull RegisterUserUseCase.Input input) {
        RegisterUserRequest regUserReq = input.user;

        validation.validate(regUserReq);
        final var user = createUserAction.invoke(regUserReq.getFursonaName());
        createAuthenticationAction.invoke(
            user.getId(),
            regUserReq.getEmail(),
            regUserReq.getPassword()
        );
        addMembershipInfoAction.invoke(
            user.getId(),
            regUserReq.getPersonalUserInformation(),
            input.event
        );

        return user;
    }

    public record Input(@NotNull RegisterUserRequest user, @Nullable Event event){}
}
