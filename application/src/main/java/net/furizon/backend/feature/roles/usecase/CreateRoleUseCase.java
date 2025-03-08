package net.furizon.backend.feature.roles.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.roles.action.createRole.CreateRoleAction;
import net.furizon.backend.feature.roles.dto.CreateRoleRequest;
import net.furizon.backend.feature.roles.dto.RoleIdResponse;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateRoleUseCase implements UseCase<CreateRoleRequest, RoleIdResponse> {
    @NotNull private final CreateRoleAction createRoleAction;

    @Override
    public @NotNull RoleIdResponse executor(@NotNull CreateRoleRequest input) {
        log.info("Creating role: '{}'", input.getInternalName());
        return new RoleIdResponse(createRoleAction.invoke(input.getInternalName()));
    }
}
