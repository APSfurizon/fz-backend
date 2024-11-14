package net.furizon.backend.feature.authentication.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.authentication.action.createAuthentication.CreateAuthenticationAction;
import net.furizon.backend.feature.authentication.dto.RegisterUserRequest;
import net.furizon.backend.feature.authentication.validation.RegisterUserValidation;
import net.furizon.backend.feature.membership.action.addMembershipInfo.AddMembershipInfoAction;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.action.createUser.CreateUserAction;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterUserUseCase implements UseCase<RegisterUserRequest, User> {
    private final RegisterUserValidation validation;

    private final CreateUserAction createUserAction;

    private final CreateAuthenticationAction createAuthenticationAction;

    private final AddMembershipInfoAction addMembershipInfoAction;

    @Transactional
    @Override
    public @NotNull User executor(@NotNull RegisterUserRequest input) {
        validation.validate(input);
        final var user = createUserAction.invoke(input.getFursonaName());
        createAuthenticationAction.invoke(
            user.getId(),
            input.getEmail(),
            input.getPassword()
        );
        addMembershipInfoAction.invoke(
            user.getId(),
            input.getPersonalUserInformation()
        );

        return user;
    }
}
