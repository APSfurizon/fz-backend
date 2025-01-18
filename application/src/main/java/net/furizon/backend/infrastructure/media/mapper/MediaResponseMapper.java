package net.furizon.backend.infrastructure.media.mapper;

import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import org.jooq.Record;

import static net.furizon.jooq.generated.tables.Media.MEDIA;

public class MediaResponseMapper {
    public static MediaResponse map(Record record) {
        return new MediaResponse(
                record.get(MEDIA.MEDIA_ID),
                record.get(MEDIA.MEDIA_PATH),
                record.get(MEDIA.MEDIA_TYPE)
        );
    }

    public static MediaResponse map(MediaData data) {
        return new MediaResponse(
                data.getId(),
                data.getPath(),
                data.getMediaType()
        );
    }
}
