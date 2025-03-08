package net.furizon.backend.feature.roles.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.roles.action.deleteRole.DeleteRoleAction;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteRoleUseCase implements UseCase<Long, Boolean> {
    @NotNull private final DeleteRoleAction deleteRoleAction;

    @Override
    public @NotNull Boolean executor(@NotNull Long input) {
        return deleteRoleAction.invoke(input);
    }
}
