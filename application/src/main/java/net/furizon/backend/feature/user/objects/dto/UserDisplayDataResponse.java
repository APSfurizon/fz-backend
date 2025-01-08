package net.furizon.backend.feature.user.objects.dto;

import lombok.Data;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.Role;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@Data
public class UserDisplayDataResponse {
    @NotNull
    private final UserDisplayData display;

    @NotNull
    private final List<Role> roles;

    @NotNull
    private final Set<Permission> permissions;

    public UserDisplayDataResponse(
            @NotNull UserDisplayData user, @NotNull List<Role> roles, @NotNull Set<Permission> permissions) {
        this.display = user;
        this.roles = roles;
        this.permissions = permissions;
    }
}
