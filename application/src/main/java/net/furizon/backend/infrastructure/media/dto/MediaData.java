package net.furizon.backend.infrastructure.media.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.backend.infrastructure.media.mapper.MediaResponseMapper;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import java.util.Objects;

import static net.furizon.jooq.generated.Tables.MEDIA;

@Data
@RequiredArgsConstructor
@Builder
public class MediaData {
    @NotNull
    private final Long id;

    @NotNull
    private final String path;

    @NotNull
    private final String mediaType;

    @NotNull
    private final StoreMethod storeMethod;

    @NotNull
    public String getFullPath() {
        return MediaData.getFullPath(storeMethod, path);
    }

    @NotNull
    public static String getFullPath(@NotNull StoreMethod storeMethod, @NotNull String path) {
        return storeMethod.getUrl(path);
    }

    @NotNull
    public static String getFullPath(@NotNull Record record) {
        return getFullPath(
                Objects.requireNonNull(StoreMethod.get(record.get(MEDIA.MEDIA_STORE_METHOD))),
                record.get(MEDIA.MEDIA_PATH)
        );
    }

    @NotNull
    public MediaResponse toMediaResponse() {
        return MediaResponseMapper.map(this);
    }
}
