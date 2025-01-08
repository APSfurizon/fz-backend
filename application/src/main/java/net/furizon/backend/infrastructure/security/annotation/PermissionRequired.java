package net.furizon.backend.infrastructure.security.annotation;

import net.furizon.backend.infrastructure.security.permissions.Permission;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@PreAuthorize("isAuthenticated()")
public @interface PermissionRequired {
    Permission[] permissions();

    PermissionRequiredMode mode() default PermissionRequiredMode.ALL;
}
