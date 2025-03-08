package net.furizon.backend.infrastructure.security.permissions.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.Role;
import net.furizon.backend.infrastructure.security.permissions.dto.JooqPermission;
import net.furizon.backend.infrastructure.security.permissions.mapper.JooqPermissionMapper;
import net.furizon.backend.infrastructure.security.permissions.mapper.JooqRoleMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.SelectJoinStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.furizon.jooq.generated.Tables.PERMISSION;
import static net.furizon.jooq.generated.Tables.ROLES;
import static net.furizon.jooq.generated.Tables.USER_HAS_ROLE;

@Component
@RequiredArgsConstructor
public class JooqPermissionFinder implements PermissionFinder {
    @NotNull
    private final SqlQuery sqlQuery;

    @Override
    public @NotNull Set<Permission> getUserPermissions(long userId) {
        return sqlQuery.fetch(
                PostgresDSL.selectDistinct(PERMISSION.PERMISSION_VALUE)
                .from(USER_HAS_ROLE)
                .innerJoin(PERMISSION)
                .on(
                    PERMISSION.ROLE_ID.eq(USER_HAS_ROLE.ROLE_ID)
                    .and(USER_HAS_ROLE.USER_ID.eq(userId))
                )
            )
            .stream()
            .map((it) -> Permission.get(it.get(PERMISSION.PERMISSION_VALUE)))
            .collect(Collectors.toSet());
    }

    @Override
    public @NotNull List<JooqPermission> getPermissionsFromRoleId(long roleId) {
        return sqlQuery.fetch(
            selectPermission()
            .where(PERMISSION.ROLE_ID.eq(roleId))
        ).stream().map(JooqPermissionMapper::map).toList();
    }

    @Override
    public @NotNull List<JooqPermission> getPermissionsFromRoleInternalName(@NotNull String roleInternalName) {
        return sqlQuery.fetch(
            selectPermission()
            .innerJoin(ROLES)
            .on(
                ROLES.ROLE_ID.eq(PERMISSION.ROLE_ID)
                .and(ROLES.INTERNAL_NAME.eq(roleInternalName))
            )
        ).stream().map(JooqPermissionMapper::map).toList();
    }

    @Override
    public @Nullable Role getRoleFromId(long roleId) {
        return sqlQuery.fetchFirst(
            selectRole()
            .where(ROLES.ROLE_ID.eq(roleId))
        ).mapOrNull(JooqRoleMapper::map);
    }

    @Override
    public @Nullable Role getRoleFromInternalName(@NotNull String roleInternalName) {
        return sqlQuery.fetchFirst(
            selectRole()
            .where(ROLES.INTERNAL_NAME.eq(roleInternalName))
        ).mapOrNull(JooqRoleMapper::map);
    }

    @Override
    public @NotNull List<Role> getRolesFromUserId(long userId) {
        return sqlQuery.fetch(
            selectRole()
            .innerJoin(USER_HAS_ROLE)
            .on(
                USER_HAS_ROLE.ROLE_ID.eq(ROLES.ROLE_ID)
                .and(USER_HAS_ROLE.USER_ID.eq(userId))
            )
        ).stream().map(JooqRoleMapper::map).toList();
    }

    @Override
    public @NotNull List<Long> getUsersWithRole(@NotNull String roleInternalName) {
        return sqlQuery.fetch(
            PostgresDSL.selectDistinct(
                USER_HAS_ROLE.USER_ID
            ).from(USER_HAS_ROLE)
            .innerJoin(ROLES)
            .on(
                ROLES.ROLE_ID.eq(USER_HAS_ROLE.ROLE_ID)
                .and(ROLES.INTERNAL_NAME.eq(roleInternalName))
            )
        ).stream().map(r -> r.get(USER_HAS_ROLE.USER_ID)).toList();
    }

    @Override
    public @NotNull List<Long> getUsersWithPermission(@NotNull Permission permission) {
        return sqlQuery.fetch(
            PostgresDSL.selectDistinct(
                USER_HAS_ROLE.USER_ID
            ).from(USER_HAS_ROLE)
            .innerJoin(ROLES)
            .on(ROLES.ROLE_ID.eq(USER_HAS_ROLE.ROLE_ID))
            .innerJoin(PERMISSION)
            .on(
                ROLES.ROLE_ID.eq(PERMISSION.ROLE_ID)
                .and(PERMISSION.PERMISSION_VALUE.eq(permission.getValue()))
            )
        ).stream().map(r -> r.get(USER_HAS_ROLE.USER_ID)).toList();
    }

    @Override
    public boolean userHasRole(long userId, long roleId) {
        return sqlQuery.fetchFirst(
            PostgresDSL.select(
                USER_HAS_ROLE.USER_ID
            )
            .from(USER_HAS_ROLE)
            .where(
                USER_HAS_ROLE.USER_ID.eq(userId)
                .and(USER_HAS_ROLE.ROLE_ID.eq(roleId))
            )
        ).isPresent();
    }

    @Override
    public boolean userHasRole(long userId, @NotNull String roleInternalName) {
        return sqlQuery.fetchFirst(
            PostgresDSL.select(
                USER_HAS_ROLE.USER_ID
            )
            .from(USER_HAS_ROLE)
            .innerJoin(ROLES)
            .on(
                ROLES.ROLE_ID.eq(USER_HAS_ROLE.ROLE_ID)
                .and(USER_HAS_ROLE.USER_ID.eq(userId))
                .and(ROLES.INTERNAL_NAME.eq(roleInternalName))
            )
        ).isPresent();
    }

    @Override
    public boolean userHasPermission(long userId, @NotNull Permission permission) {
        return sqlQuery.fetchFirst(
            PostgresDSL.select(
                USER_HAS_ROLE.USER_ID
            )
            .from(USER_HAS_ROLE)
            .innerJoin(PERMISSION)
            .on(
                PERMISSION.ROLE_ID.eq(USER_HAS_ROLE.ROLE_ID)
                .and(USER_HAS_ROLE.USER_ID.eq(userId))
                .and(PERMISSION.PERMISSION_VALUE.eq(permission.getValue()))
            )
        ).isPresent();
    }


    private @NotNull SelectJoinStep<Record2<Long, Long>> selectPermission() {
        return PostgresDSL.select(
            PERMISSION.PERMISSION_VALUE,
            PERMISSION.ROLE_ID
        ).from(PERMISSION);
    }

    private @NotNull SelectJoinStep<Record3<Long, String, String>> selectRole() {
        return PostgresDSL.select(
            ROLES.ROLE_ID,
            ROLES.DISPLAY_NAME,
            ROLES.INTERNAL_NAME
        ).from(ROLES);
    }
}
