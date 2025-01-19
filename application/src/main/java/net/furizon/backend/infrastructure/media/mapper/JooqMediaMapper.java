package net.furizon.backend.infrastructure.media.mapper;

import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.infrastructure.media.StoreMethod;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import java.util.Objects;

import static net.furizon.jooq.generated.Tables.MEDIA;

public class JooqMediaMapper {
    @NotNull
    public static MediaData map(Record record) {
        return MediaData.builder()
                .id(record.get(MEDIA.MEDIA_ID))
                .mediaType(record.get(MEDIA.MEDIA_TYPE))
                .path(record.get(MEDIA.MEDIA_PATH))
                .storeMethod(Objects.requireNonNull(StoreMethod.get(record.get(MEDIA.MEDIA_STORE_METHOD))))
                .build();
    }
}
