package net.furizon.backend.infrastructure.media.mapper;

import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;

import java.util.Objects;
import java.util.function.Function;

import static net.furizon.jooq.generated.Tables.MEDIA;

public class MediaResponseMapper {
    @Nullable
    public static MediaResponse mapOrNull(Record record) {
        return mapOrNull(record, null);
    }
    @Nullable
    public static MediaResponse mapOrNull(Record record, @Nullable Table<?> table) {
        Long mediaId;
        String mediaPath;
        String mediaType;
        Integer storage;

        if (table == null) {
            mediaId = record.get(MEDIA.MEDIA_ID);
            mediaPath = record.get(MEDIA.MEDIA_PATH);
            mediaType = record.get(MEDIA.MEDIA_TYPE);
            storage = record.get(MEDIA.MEDIA_STORE_METHOD);
        } else {
            mediaId = record.get(table.field(MEDIA.MEDIA_ID));
            mediaPath = record.get(table.field(MEDIA.MEDIA_PATH));
            mediaType = record.get(table.field(MEDIA.MEDIA_TYPE));
            storage = record.get(table.field(MEDIA.MEDIA_STORE_METHOD));
        }

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
    public static MediaResponse map(Record record, @Nullable Table<?> table) {
        return Objects.requireNonNull(mapOrNull(record, table));
    }
    @NotNull
    public static MediaResponse map(Record record) {
        return Objects.requireNonNull(mapOrNull(record));
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
