package net.furizon.backend.infrastructure.security.permissions.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.roles.dto.ListedRoleResponse;
import net.furizon.backend.feature.roles.mapper.ListedRoleMapper;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.Role;
import net.furizon.backend.infrastructure.security.permissions.dto.JooqPermission;
import net.furizon.backend.infrastructure.security.permissions.mapper.JooqPermissionMapper;
import net.furizon.backend.infrastructure.security.permissions.mapper.JooqRoleMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;
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
        return sqlQuery
            .fetch(
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
            ).from(USER_HAS_ROLE)
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

    @Override
    public @NotNull List<ListedRoleResponse> listPermissions() {
        /*
SELECT
    x.role_id,
    x.temp_users,
    x.permanent_users,
    z.permissions,
    roles.display_name,
    roles.internal_name--,
    --roles.show_in_nosecount
FROM (
    SELECT
        y.role_id,
        SUM(users) FILTER (WHERE temp_role = TRUE) AS temp_users,
        SUM(users) FILTER (WHERE temp_role = FALSE) AS permanent_users
    FROM (
        SELECT
            user_has_role.role_id,
            (user_has_role.temp_event_id IS NOT NULL) AS temp_role,
            COUNT(DISTINCT CAST(user_has_role.user_id AS TEXT) || ' ' || user_has_role.role_id::int8) AS users
        FROM
            user_has_role
        GROUP BY user_has_role.role_id, (user_has_role.temp_event_id IS NOT NULL)
    ) AS y
    GROUP BY y.role_id
) AS x
INNER JOIN roles
ON
    x.role_id = roles.role_id
INNER JOIN (
     SELECT
         permission.role_id,
         COUNT(DISTINCT permission.role_id::int8 || ' ' || permission.permission_value::int8) AS permissions
     FROM
         permission
     GROUP BY permission.role_id
) AS z
ON
    roles.role_id = z.role_id;
         */
        Field<Boolean> tempRole = PostgresDSL.field("temp_role", Boolean.class);
        Field<Long> permissionCount = PostgresDSL.field("permissions", Long.class);
        Field<Long> usersCount = PostgresDSL.field("users", Long.class);
        Field<Long> tempUsers = PostgresDSL.field("temp_users", Long.class);
        Field<Long> permanentUsers = PostgresDSL.field("permanent_users", Long.class);

        Field<Boolean> tempRoleQuery = USER_HAS_ROLE.TEMP_EVENT_ID.isNotNull().as(tempRole);
        Table<?> usersCountTable = PostgresDSL.select(
                USER_HAS_ROLE.ROLE_ID,
                tempRoleQuery,
                PostgresDSL.countDistinct(
                    PostgresDSL.concat(PostgresDSL.concat(
                        USER_HAS_ROLE.USER_ID.cast(SQLDataType.CLOB),
                        " "),
                        USER_HAS_ROLE.ROLE_ID.cast(SQLDataType.CLOB)
                    )
                ).as(usersCount)
            )
            .from(USER_HAS_ROLE)
            .groupBy(USER_HAS_ROLE.ROLE_ID, tempRoleQuery)
            .asTable("count_users_table");



        Table<?> usersPerRoleTable = PostgresDSL.select(
                usersCountTable.field(USER_HAS_ROLE.ROLE_ID),
                PostgresDSL.sum(usersCount).filterWhere(tempRole.isTrue()).as(tempUsers),
                PostgresDSL.sum(usersCount).filterWhere(tempRole.isFalse()).as(permanentUsers)
            )
            .from(usersCountTable)
            .groupBy(usersCountTable.field(USER_HAS_ROLE.ROLE_ID))
            .asTable("users_per_role_table");

        Table<?> permissionsPerRoleTable = PostgresDSL.select(
                PERMISSION.ROLE_ID,
                PostgresDSL.countDistinct(
                    PostgresDSL.concat(PostgresDSL.concat(
                        PERMISSION.PERMISSION_VALUE.cast(SQLDataType.CLOB),
                        " "),
                        PERMISSION.ROLE_ID.cast(SQLDataType.CLOB)
                    )
                ).as(permissionCount)
            )
            .from(PERMISSION)
            .groupBy(PERMISSION.ROLE_ID)
            .asTable("permissions_per_role_table");


        return sqlQuery.fetch(
            PostgresDSL.select(
                ROLES.ROLE_ID,
                ROLES.INTERNAL_NAME,
                ROLES.DISPLAY_NAME,
                ROLES.SHOW_IN_NOSECOUNT,
                permissionsPerRoleTable.field(permissionCount),
                usersPerRoleTable.field(tempUsers),
                usersPerRoleTable.field(permanentUsers)
            )
            .from(ROLES)
            .innerJoin(usersPerRoleTable)
            .on(ROLES.ROLE_ID.eq(usersPerRoleTable.field(USER_HAS_ROLE.ROLE_ID)))
            .innerJoin(permissionsPerRoleTable)
            .on(ROLES.ROLE_ID.eq(permissionsPerRoleTable.field(PERMISSION.ROLE_ID)))
        ).stream().map(r -> ListedRoleMapper.map(
            r,
            permissionsPerRoleTable.field(permissionCount),
            usersPerRoleTable.field(tempUsers),
            usersPerRoleTable.field(permanentUsers)
        )).toList();
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
