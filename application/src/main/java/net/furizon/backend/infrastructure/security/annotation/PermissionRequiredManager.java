package net.furizon.backend.infrastructure.security.annotation;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class PermissionRequiredManager implements AuthorizationManager<MethodInvocation> {
    private static final AuthorizationDecision SUCCESS_AUTHORIZATION_DECISION = new AuthorizationDecision(true);

    @NotNull private final PermissionFinder permissionFinder;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocation object) {
        final var annotation = AnnotationUtils.findAnnotation(object.getMethod(), PermissionRequired.class);
        if (annotation == null) {
            //log.error("Permission required annotation not found");
            return null;
        }
        final var v = authentication.get();
        final FurizonUser user = v != null ? (FurizonUser) v.getPrincipal() : null;
        if (user == null) {
            log.error("User is not logged in while running {}", object.getMethod().getName());
            throw new AccessDeniedException("User is not logged in");
        }
        final Permission[] requiredPermissions = annotation.permissions();
        final Set<Permission> userPermissions = permissionFinder.getUserPermissions(user.getUserId());

        String methodFullName = object.getMethod().getDeclaringClass().getName()
                + "." + object.getMethod().getName() + "()";

        return switch (annotation.mode()) {
            case ALL -> {
                if (!Arrays.stream(requiredPermissions).allMatch(userPermissions::contains)) {
                    String permissions = StringUtils.join(annotation.permissions(), ',');
                    log.error("User {} running {} doesn't have all required permissions: {}",
                            user.getUserId(), methodFullName, permissions);
                    throw new AccessDeniedException("User must have all permissions: %s".formatted(permissions));
                }
                log.info("User {} was authorized to use method {}", user.getUserId(), methodFullName);
                yield SUCCESS_AUTHORIZATION_DECISION;
            }
            case ANY -> {
                if (Arrays.stream(requiredPermissions).noneMatch(userPermissions::contains)) {
                    String permissions = StringUtils.join(annotation.permissions(), ',');
                    log.error("User {} running {} doesn't have any required permissions: {}",
                            user.getUserId(), methodFullName, permissions);
                    throw new AccessDeniedException(
                            "User must have at least one permission: %s".formatted(permissions));
                }

                log.info("User {} was authorized to use method {}", user.getUserId(), methodFullName);
                yield SUCCESS_AUTHORIZATION_DECISION;
            }
        };
    }
}
