package net.furizon.backend.infrastructure.security.annotation;

import net.furizon.backend.infrastructure.security.permissions.Permission;

public @interface PermissionRequired {
    Permission[] permissions();

    PermissionRequiredMode mode() default PermissionRequiredMode.ALL;
}
