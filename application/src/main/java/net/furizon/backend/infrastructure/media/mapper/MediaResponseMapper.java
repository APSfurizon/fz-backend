package net.furizon.backend.infrastructure.media.mapper;

import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;

import java.util.Objects;

import static net.furizon.jooq.generated.Tables.MEDIA;

public class MediaResponseMapper {
    @Nullable
    public static MediaResponse mapOrNull(Record record) {
        Long mediaId = record.get(MEDIA.MEDIA_ID);
        String mediaPath = record.get(MEDIA.MEDIA_PATH);
        String mediaType = record.get(MEDIA.MEDIA_TYPE);
        Integer storage = record.get(MEDIA.MEDIA_STORE_METHOD);

        if (mediaId == null || mediaPath == null || mediaType == null || storage == null) {
            return null;
        }

        return new MediaResponse(
                mediaId,
                MediaData.getFullPath(Objects.requireNonNull(StoreMethod.get(storage)), mediaPath),
                mediaType
        );
    }

    @NotNull
    public static MediaResponse map(Record record) {
        return new MediaResponse(
                record.get(MEDIA.MEDIA_ID),
                MediaData.getFullPath(record),
                record.get(MEDIA.MEDIA_TYPE)
        );
    }

    @NotNull
    public static MediaResponse map(MediaData data) {
        return new MediaResponse(
                data.getId(),
                data.getFullPath(),
                data.getMediaType()
        );
    }
}
