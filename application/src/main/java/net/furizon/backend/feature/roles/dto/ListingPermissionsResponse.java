package net.furizon.backend.feature.roles.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.furizon.backend.infrastructure.security.permissions.Permission;

import java.util.List;

@Data
public class ListingPermissionsResponse {
    public static final ListingPermissionsResponse INSTANCE = new ListingPermissionsResponse();

    @NotNull private final List<Permission> permissions = List.of(Permission.values());
}
