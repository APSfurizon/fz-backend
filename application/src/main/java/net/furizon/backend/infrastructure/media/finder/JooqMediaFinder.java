package net.furizon.backend.infrastructure.media.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.badge.dto.MediaData;
import net.furizon.backend.feature.badge.mapper.JooqMediaMapper;
import net.furizon.jooq.generated.Public;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.HashSet;
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
            selectMedia()
            .where(MEDIA.MEDIA_ID.in(ids))
        ).stream().map(JooqMediaMapper::map).toList();
    }

    @Override
    public @NotNull List<MediaData> findAll() {
        return sqlQuery.fetch(selectMedia()).stream().map(JooqMediaMapper::map).toList();
    }

    @Override
    public @NotNull Set<Long> getReferencedMediaIds() {
        //Beware from copying this code around:
        // keep in mind this currently works only for pk made by
        // a single field (like media does)

        Set<Long> ids = new HashSet<>();

        List<Table<?>> tables = Public.PUBLIC.getTables();
        for (Table<?> table : tables) {
            List<? extends ForeignKey<?, Record>> references = MEDIA.getReferencesFrom(table);
            for (ForeignKey<?, ?> fk : references) {
                for (Field<?> fkField : fk.getFields()) {

                    ids.addAll(sqlQuery.fetch(
                        PostgresDSL.selectDistinct(fkField)
                        .from(table)
                        .where(fkField.isNotNull())
                    ).stream().map(r -> (Long) r.getValue(fkField)).toList());
                }
            }
        }

        return ids;
    }

    @Override
    public @NotNull SelectJoinStep<Record3<Long, String, String>> selectMedia() {
        return PostgresDSL.select(
            MEDIA.MEDIA_ID,
            MEDIA.MEDIA_TYPE,
            MEDIA.MEDIA_PATH,
            MEDIA.
        ).from(MEDIA);
    }
}
