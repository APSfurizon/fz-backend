package net.furizon.backend.feature.gallery.mapper;

import net.furizon.backend.feature.gallery.dto.UploadProgress;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.UPLOAD_PROGRESS_INFO;

public class UploadProgressMapper {
    public static UploadProgress map(@NotNull Record record) {
        return UploadProgress.builder()
                .uploadReqId(record.get(UPLOAD_PROGRESS_INFO.ID))
                .uploadId(record.get(UPLOAD_PROGRESS_INFO.S3_UPLOAD_ID))
                .s3Key(record.get(UPLOAD_PROGRESS_INFO.KEY_NAME))
                .expireTs(record.get(UPLOAD_PROGRESS_INFO.EXPIRE_TS))
                .size(record.get(UPLOAD_PROGRESS_INFO.SIZE))
                .uploaderUserId(record.get(UPLOAD_PROGRESS_INFO.UPLOADER_USER_ID))
            .build();
    }
}
