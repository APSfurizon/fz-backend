package net.furizon.backend.feature.badge.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.badge.dto.BadgeUploadResponse;
import net.furizon.backend.feature.badge.dto.MediaData;
import net.furizon.backend.feature.badge.mapper.JooqMediaMapper;
import net.furizon.jooq.generated.tables.Media;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static net.furizon.jooq.generated.Tables.MEDIA;

@Component
@RequiredArgsConstructor
public class JooqMediaFinder implements MediaFinder {
    @NotNull
    private final SqlQuery sqlQuery;
    @Override
    public @Nullable Set<MediaData> findByIds(Set<Long> ids) {
        return sqlQuery
            .fetch(
            PostgresDSL
            .select(
                MEDIA.MEDIA_ID,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_PATH
            )
            .from(MEDIA)
            .where(MEDIA.MEDIA_ID.in(ids))
        ).stream()
        .map(JooqMediaMapper::map)
        .collect(Collectors.toSet());
    }
}
