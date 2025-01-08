package net.furizon.backend.feature.user.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.feature.user.objects.dto.UserDisplayDataResponse;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.Role;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetUserDisplayDataUseCase implements UseCase<GetUserDisplayDataUseCase.Input,
        Optional<UserDisplayDataResponse>> {
    @NotNull private final UserFinder userFinder;
    @NotNull private final PermissionFinder permissionFinder;

    @Override
    public @NotNull Optional<UserDisplayDataResponse> executor(@NotNull GetUserDisplayDataUseCase.Input input) {
        User userFound = userFinder.findById(input.userId);
        List<Role> roles = permissionFinder.getRolesFromUserId(input.userId);
        Set<Permission> permissions = permissionFinder.getUserPermissions(input.userId);
        return Optional.ofNullable(
            userFound != null ? new UserDisplayDataResponse(userFound, roles, permissions) : null
        );
    }

    public record Input(long userId) {}
}
