package net.furizon.backend.infrastructure.security.permissions.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.roles.dto.ListedRoleResponse;
import net.furizon.backend.feature.roles.dto.UserHasRoleResponse;
import net.furizon.backend.feature.roles.mapper.ListedRoleMapper;
import net.furizon.backend.feature.roles.mapper.UserHasRoleMapper;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.Role;
import net.furizon.backend.infrastructure.security.permissions.dto.JooqPermission;
import net.furizon.backend.infrastructure.security.permissions.dto.JooqUserHasRole;
import net.furizon.backend.infrastructure.security.permissions.mapper.JooqPermissionMapper;
import net.furizon.backend.infrastructure.security.permissions.mapper.JooqRoleMapper;
import net.furizon.backend.infrastructure.security.permissions.mapper.JooqUserHasRoleMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record5;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.PERMISSION;
import static net.furizon.jooq.generated.Tables.ROLES;
import static net.furizon.jooq.generated.Tables.USERS;
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
    public @NotNull List<Long> getUsersWithRoleInternalName(@NotNull String roleInternalName) {
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
    public @NotNull List<Long> getUsersWithRoleId(long roleId) {
        return sqlQuery.fetch(
            PostgresDSL.selectDistinct(
                USER_HAS_ROLE.USER_ID
            ).from(USER_HAS_ROLE)
            .where(USER_HAS_ROLE.ROLE_ID.eq(roleId))
        ).stream().map(r -> r.get(USER_HAS_ROLE.USER_ID)).toList();
    }

    @Override
    public @NotNull List<UserHasRoleResponse> getDisplayUsersWithRoleId(long roleId) {
        return sqlQuery.fetch(
            PostgresDSL.select(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                USERS.USER_LOCALE,
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_ID,
                USER_HAS_ROLE.TEMP_EVENT_ID
            )
            .from(USERS)
            .innerJoin(USER_HAS_ROLE)
            .on(
                USERS.USER_ID.eq(USER_HAS_ROLE.USER_ID)
                .and(USER_HAS_ROLE.ROLE_ID.eq(roleId))
            )
            .leftJoin(MEDIA)
            .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
        ).stream().map(UserHasRoleMapper::map).toList();
    }

    @Override
    public @NotNull List<JooqUserHasRole> getUserHasRoleByRoleId(long roleId) {
        return sqlQuery.fetch(
            PostgresDSL.select(
                USER_HAS_ROLE.USER_ID,
                USER_HAS_ROLE.ROLE_ID,
                USER_HAS_ROLE.TEMP_EVENT_ID
            )
            .from(USER_HAS_ROLE)
            .where(USER_HAS_ROLE.ROLE_ID.eq(roleId))
        ).stream().map(JooqUserHasRoleMapper::map).toList();
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

    @Override
    public @NotNull List<ListedRoleResponse> listPermissions() {
        Field<Boolean> tempRole = PostgresDSL.field("temp_role", Boolean.class);
        Field<Integer> permissionCount = PostgresDSL.field("permissions", Integer.class);
        Field<Long> usersCount = PostgresDSL.field("users", Long.class);
        Field<BigDecimal> tempUsers = PostgresDSL.field("temp_users", BigDecimal.class);
        Field<BigDecimal> permanentUsers = PostgresDSL.field("permanent_users", BigDecimal.class);

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
                PostgresDSL.coalesce(permissionsPerRoleTable.field(permissionCount), 0L).as(permissionCount),
                PostgresDSL.coalesce(usersPerRoleTable.field(tempUsers), 0L).as(tempUsers),
                PostgresDSL.coalesce(usersPerRoleTable.field(permanentUsers), 0L).as(permanentUsers)
            )
            .from(ROLES)
            .leftJoin(usersPerRoleTable)
            .on(ROLES.ROLE_ID.eq(usersPerRoleTable.field(USER_HAS_ROLE.ROLE_ID)))
            .leftJoin(permissionsPerRoleTable)
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

    private @NotNull SelectJoinStep<Record5<Long, String, String, Boolean, Long>> selectRole() {
        return PostgresDSL.select(
            ROLES.ROLE_ID,
            ROLES.DISPLAY_NAME,
            ROLES.INTERNAL_NAME,
            ROLES.SHOW_IN_NOSECOUNT,
            ROLES.ROLE_ADMINCOUNT_PRIORITY
        ).from(ROLES);
    }
}
