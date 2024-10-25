package net.furizon.backend.feature.user.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.mapper.JooqUserMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.USERS;

@Component
@RequiredArgsConstructor
public class JooqUserFinder implements UserFinder {
    private final SqlQuery sqlQuery;

    @NotNull
    @Override
    public List<User> getAllUsers() {
        return sqlQuery
            .fetch(selectUser())
            .stream()
            .map(JooqUserMapper::map)
            .toList();
    }

    @Nullable
    @Override
    public User findById(long id) {
        return sqlQuery
            .fetchFirst(
                selectUser()
                    .where(USERS.USER_ID.eq(id))
            )
            .mapOrNull(JooqUserMapper::map);
    }

    @Nullable
    @Override
    public User findBySecret(String secret) {
        return sqlQuery
                .fetchFirst(
                        selectUser()
                                .where(USERS.USER_SECRET.eq(secret))
                )
                .mapOrNull(JooqUserMapper::map);
    }

    private SelectJoinStep<?> selectUser() {
        return DSL
            .select(USERS.USER_ID)
            .from(USERS);
    }
}
