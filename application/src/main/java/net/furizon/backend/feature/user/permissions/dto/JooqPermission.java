package net.furizon.backend.feature.user.permissions.dto;

import lombok.Data;
import net.furizon.backend.feature.user.permissions.Permission;
import org.jetbrains.annotations.NotNull;

@Data
public class JooqPermission {
    private final long roleId;
    @NotNull private final Permission permission;
}
