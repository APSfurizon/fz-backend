package net.furizon.backend.feature.user.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.User;
import net.furizon.backend.feature.user.dto.SearchUsersResponse;
import net.furizon.backend.feature.user.mapper.JooqSearchUserMapper;
import net.furizon.backend.feature.user.mapper.JooqUserMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SelectJoinStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.USERS;

@Component
@RequiredArgsConstructor
public class JooqUserFinder implements UserFinder {
    private final SqlQuery sqlQuery;

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

    public List<SearchUsersResponse.SearchUser> searchUserInCurrentEvent(
            @NotNull String fursonaName, @NotNull Event event
    ) {
        return sqlQuery.fetch(
            PostgresDSL
            .select(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                MEDIA.MEDIA_PATH
            )
            .from(USERS)
            .innerJoin(MEDIA)
            .on(
                USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID)
                .and(
                    USERS.USER_FURSONA_NAME.like("%" + fursonaName + "%")
                    .and(USERS.SHOW_IN_NOSECOUNT.eq(true))
                    .or(
                        //If someone doesn't want to be displayed in the nosecount,
                        // find him only if it's a almost exact match
                        USERS.USER_FURSONA_NAME.like("_" + fursonaName + "_")
                        .and(USERS.SHOW_IN_NOSECOUNT.eq(false))
                    )
                )
            )
            .orderBy(
                PostgresDSL.position(fursonaName, USERS.USER_FURSONA_NAME),
                USERS.USER_FURSONA_NAME
            )
        ).stream().map(JooqSearchUserMapper::map).toList();
    }

    private SelectJoinStep<?> selectUser() {
        return PostgresDSL
            .select(
                    USERS.USER_ID,
                    USERS.USER_FURSONA_NAME,
                    USERS.USER_LOCALE,
                    USERS.MEDIA_ID_PROPIC,
                    USERS.SHOW_IN_NOSECOUNT
            )
            .from(USERS);
    }
}
