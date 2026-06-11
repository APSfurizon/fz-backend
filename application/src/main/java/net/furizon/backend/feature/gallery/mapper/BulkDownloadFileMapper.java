package net.furizon.backend.feature.gallery.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDownloadFile;
import net.furizon.backend.feature.pretix.objects.event.mapper.JooqEventMapper;
import net.furizon.backend.infrastructure.localization.TranslationService;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import java.util.Map;

import static net.furizon.jooq.generated.Tables.EVENTS;
import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.UPLOADS;
import static net.furizon.jooq.generated.Tables.USERS;

public class BulkDownloadFileMapper {
    public static BulkDownloadFile map(@NotNull Record r,
                                       @NotNull ObjectMapper mapper,
                                       @NotNull TranslationService translationService) {
        Map<String, String> eventNames = JooqEventMapper.mapEventNames(r, mapper);
        long eventId = r.get(EVENTS.ID);
        String eventName = eventNames == null
                         ? String.format("Event-%03d", eventId)
                         : translationService.getTranslationFromMap(eventNames);

        return BulkDownloadFile.builder()
                .s3Key(r.get(MEDIA.MEDIA_PATH))
                .uploadTs(r.get(UPLOADS.UPLOAD_TIMESTAMP))
                .fileName(r.get(UPLOADS.ORIGINAL_FILE_NAME))
                .fileSize(r.get(UPLOADS.FILE_SIZE))
                .eventName(eventName)
                .eventId(eventId)
                .photographerName(r.get(USERS.USER_FURSONA_NAME))
                .photographerId(r.get(USERS.USER_ID))
            .build();
    }
}
