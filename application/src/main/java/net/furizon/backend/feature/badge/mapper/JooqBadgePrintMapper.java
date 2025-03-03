package net.furizon.backend.feature.badge.mapper;

import net.furizon.backend.feature.badge.dto.BadgePrint;
import net.furizon.backend.infrastructure.media.StoreMethod;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import java.util.Objects;

import static net.furizon.jooq.generated.Tables.MEDIA;

public class JooqBadgePrintMapper {
    @NotNull
    public static BadgePrint map(Record record) {
        return BadgePrint.builder()
                .nickname(record.get(MEDIA.MEDIA_ID))
                .imageUrl(record.get(MEDIA.MEDIA_TYPE))
                .staffRole(record.get(MEDIA.MEDIA_PATH))
                .locales(Objects.requireNonNull(StoreMethod.get(record.get(MEDIA.MEDIA_STORE_METHOD))))
                .build();
    }
}
