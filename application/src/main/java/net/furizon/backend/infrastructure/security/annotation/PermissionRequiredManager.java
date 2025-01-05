package net.furizon.backend.infrastructure.security.annotation;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class PermissionRequiredManager implements AuthorizationManager<MethodInvocation> {
    private static final AuthorizationDecision SUCCESS_AUTHORIZATION_DECISION = new AuthorizationDecision(true);

    private final PermissionFinder permissionFinder;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, MethodInvocation object) {
        final var annotation = AnnotationUtils.findAnnotation(object.getMethod(), PermissionRequired.class);
        if (annotation == null) {
            return null;
        }
        final var user = (FurizonUser) authentication.get().getPrincipal();
        final var requiredPermissions = annotation.permissions();
        final var userPermissions = permissionFinder.getUserPermissions(user.getUserId());

        return switch (annotation.mode()) {
            case ALL -> {
                if (Arrays.stream(requiredPermissions).allMatch(userPermissions::contains)) {
                    throw new AccessDeniedException(
                        "User must have all permissions: %s".formatted(
                            StringUtils.join(annotation.permissions(), ',')
                        )
                    );
                }

                yield SUCCESS_AUTHORIZATION_DECISION;
            }
            case ANY -> {
                if (Arrays.stream(requiredPermissions).noneMatch(userPermissions::contains)) {
                    throw new AccessDeniedException("");
                }

                yield SUCCESS_AUTHORIZATION_DECISION;
            }
        };
    }
}
