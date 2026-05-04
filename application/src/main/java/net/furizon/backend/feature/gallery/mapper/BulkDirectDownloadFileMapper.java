package net.furizon.backend.feature.gallery.mapper;

import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDirectDownloadFile;
import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import java.util.Objects;

import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.UPLOADS;

public class BulkDirectDownloadFileMapper {
    public static BulkDirectDownloadFile map(@NotNull Record r) {
        return new BulkDirectDownloadFile(
            MediaData.getFullPath(
                    Objects.requireNonNull(StoreMethod.get(r.get(MEDIA.MEDIA_STORE_METHOD))),
                    r.get(MEDIA.MEDIA_PATH)
            ),
            r.get(UPLOADS.ORIGINAL_FILE_NAME),
            r.get(UPLOADS.UPLOAD_TIMESTAMP),
            r.get(UPLOADS.FILE_SIZE)
        );
    }
}
