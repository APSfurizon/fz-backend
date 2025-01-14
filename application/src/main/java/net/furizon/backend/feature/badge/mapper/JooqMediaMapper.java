package net.furizon.backend.feature.badge.mapper;

import net.furizon.backend.feature.badge.dto.MediaData;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.MEDIA;

public class JooqMediaMapper {
    @NotNull
    public static MediaData map(Record record) {
        return MediaData.builder()
                .id(record.get(MEDIA.MEDIA_ID))
                .mediaType(record.get(MEDIA.MEDIA_TYPE))
                .relativePath(record.get(MEDIA.MEDIA_PATH))
                .build();
    }
}
