package net.furizon.backend.infrastructure.image.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.badge.dto.MediaData;
import net.furizon.backend.feature.badge.mapper.JooqMediaMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record3;
import org.jooq.SelectJoinStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static net.furizon.jooq.generated.Tables.MEDIA;

@Component
@RequiredArgsConstructor
public class JooqMediaFinder implements MediaFinder {
    @NotNull
    private final SqlQuery sqlQuery;
    @Override
    public @NotNull List<MediaData> findByIds(Set<Long> ids) {
        return sqlQuery.fetch(
            PostgresDSL.select(
                MEDIA.MEDIA_ID,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_PATH
            )
            .from(MEDIA)
            .where(MEDIA.MEDIA_ID.in(ids))
        ).stream().map(JooqMediaMapper::map).toList();
    }

    @Override
    public @NotNull SelectJoinStep<Record3<Long, String, String>> selectMedia() {
        return PostgresDSL.select(
            MEDIA.MEDIA_ID,
            MEDIA.MEDIA_TYPE,
            MEDIA.MEDIA_PATH
        ).from(MEDIA);
    }
}
