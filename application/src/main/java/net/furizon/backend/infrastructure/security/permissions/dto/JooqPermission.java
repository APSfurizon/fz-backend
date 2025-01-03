package net.furizon.backend.infrastructure.security.permissions.dto;

import lombok.Data;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import org.jetbrains.annotations.NotNull;

@Data
public class JooqPermission {
    private final long roleId;
    @NotNull private final Permission permission;
}
