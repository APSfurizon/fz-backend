package net.furizon.backend.feature.badge.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.badge.dto.MediaData;
import net.furizon.backend.infrastructure.media.mapper.JooqMediaMapper;
import net.furizon.backend.infrastructure.media.finder.MediaFinder;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.tables.Users.USERS;
import static net.furizon.jooq.generated.tables.Media.MEDIA;

@Component
@RequiredArgsConstructor
public class JooqBadgeFinder implements BadgeFinder {
    @NotNull private final MediaFinder mediaFinder;
    @NotNull private final SqlQuery sqlQuery;

    @Override
    public @Nullable MediaData getMediaDataOfUserBadge(long userId) {
        return sqlQuery.fetchFirst(
            mediaFinder.selectMedia()
            .join(USERS)
            .on(
                USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID)
                .and(USERS.USER_ID.eq(userId))
            )
        ).mapOrNull(JooqMediaMapper::map);
    }
}
