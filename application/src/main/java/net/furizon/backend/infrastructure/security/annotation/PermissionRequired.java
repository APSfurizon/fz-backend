package net.furizon.backend.infrastructure.security.annotation;

import net.furizon.backend.infrastructure.security.permissions.Permission;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@PreAuthorize("isAuthenticated()")
public @interface PermissionRequired {
    Permission[] permissions();

    PermissionRequiredMode mode() default PermissionRequiredMode.ALL;
}
