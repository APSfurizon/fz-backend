package net.furizon.backend.infrastructure.media.mapper;

import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;

import static net.furizon.jooq.generated.tables.Media.MEDIA;

public class MediaResponseMapper {
    @Nullable
    public static MediaResponse mapOrNull(Record record) {
        Long mediaId = record.get(MEDIA.MEDIA_ID);
        String mediaPath = record.get(MEDIA.MEDIA_PATH);
        String mediaType = record.get(MEDIA.MEDIA_TYPE);

        if (mediaId == null || mediaPath == null || mediaType == null) {
            return null;
        }

        return new MediaResponse(mediaId, mediaPath, mediaType);
    }

    @NotNull
    public static MediaResponse map(Record record) {
        return new MediaResponse(
                record.get(MEDIA.MEDIA_ID),
                record.get(MEDIA.MEDIA_PATH),
                record.get(MEDIA.MEDIA_TYPE)
        );
    }

    @NotNull
    public static MediaResponse map(MediaData data) {
        return new MediaResponse(
                data.getId(),
                data.getPath(),
                data.getMediaType()
        );
    }
}
