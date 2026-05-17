package net.furizon.backend.feature.gallery.action.uploadProgress.addUploadProgress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static net.furizon.jooq.generated.Tables.UPLOAD_PROGRESS_INFO;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqAddUploadProgress implements AddUploadProgressAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public long invoke(
            @NotNull String uploadId,
            @NotNull String keyName,
            @NotNull LocalDateTime expiration,
            long size,
            long userId
    ) {
        log.info("Creating new progress info obj for uploadId {} and key {}", uploadId, keyName);
        return command.executeResult(
            PostgresDSL.insertInto(
                UPLOAD_PROGRESS_INFO,
                UPLOAD_PROGRESS_INFO.S3_UPLOAD_ID,
                UPLOAD_PROGRESS_INFO.KEY_NAME,
                UPLOAD_PROGRESS_INFO.EXPIRE_TS,
                UPLOAD_PROGRESS_INFO.SIZE,
                UPLOAD_PROGRESS_INFO.UPLOADER_USER_ID
            )
            .values(
                uploadId,
                keyName,
                expiration,
                size,
                userId
            )
            .returning(UPLOAD_PROGRESS_INFO.ID)
        ).getFirst().map(r -> r.get(UPLOAD_PROGRESS_INFO.ID));
    }
}
