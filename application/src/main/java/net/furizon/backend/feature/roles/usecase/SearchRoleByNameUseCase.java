package net.furizon.backend.feature.roles.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.roles.dto.responses.RolesResponse;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchRoleByNameUseCase implements UseCase<String, RolesResponse> {
    @NotNull
    private final PermissionFinder permissionFinder;

    @Override
    public @NonNull RolesResponse executor(@NonNull String input) {
        return new RolesResponse(permissionFinder.searchRolesByName(input));
    }
}
