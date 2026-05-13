package net.furizon.backend.feature.gallery.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.gallery.dto.UploadProgress;
import net.furizon.backend.feature.gallery.mapper.UploadProgressMapper;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record6;
import org.jooq.SelectSelectStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static net.furizon.jooq.generated.Tables.UPLOAD_PROGRESS_INFO;

@Component
@RequiredArgsConstructor
public class JooqUploadProgressFinder implements UploadProgressFinder {
    @NotNull
    private final SqlQuery query;

    @Override
    public @Nullable Long getUploadingProgressIdByUser(long userId) {
        return query.fetchFirst(
            PostgresDSL.select(UPLOAD_PROGRESS_INFO.ID)
            .from(UPLOAD_PROGRESS_INFO)
            .where(UPLOAD_PROGRESS_INFO.UPLOADER_USER_ID.eq(userId))
            .limit(1)
        ).mapOrNull(record -> record.get(UPLOAD_PROGRESS_INFO.ID));
    }

    @Override
    public @NotNull List<UploadProgress> getExpiredUploadProgress() {
        return query.fetch(
            selectUploadProgress()
            .from(UPLOAD_PROGRESS_INFO)
            .where(UPLOAD_PROGRESS_INFO.EXPIRE_TS.lessThan(LocalDateTime.now()))
        ).stream().map(UploadProgressMapper::map).toList();
    }

    @Override
    public @Nullable UploadProgress getUploadProgressByUser(long userId) {
        return query.fetchFirst(
            selectUploadProgress()
            .from(UPLOAD_PROGRESS_INFO)
            .where(UPLOAD_PROGRESS_INFO.UPLOADER_USER_ID.eq(userId))
            .limit(1)
        ).mapOrNull(UploadProgressMapper::map);
    }

    @Override
    public @Nullable UploadProgress getUploadProgressByReqId(long reqId) {
        return query.fetchFirst(
            selectUploadProgress()
            .from(UPLOAD_PROGRESS_INFO)
            .where(UPLOAD_PROGRESS_INFO.ID.eq(reqId))
            .limit(1)
        ).mapOrNull(UploadProgressMapper::map);
    }

    @Override
    public @Nullable UploadProgress getUploadProgressByReqIdUser(long reqId, long userId) {
        return query.fetchFirst(
            selectUploadProgress()
            .from(UPLOAD_PROGRESS_INFO)
            .where(
                UPLOAD_PROGRESS_INFO.ID.eq(reqId)
                .and(UPLOAD_PROGRESS_INFO.UPLOADER_USER_ID.eq(userId))
            )
            .limit(1)
        ).mapOrNull(UploadProgressMapper::map);
    }


    private @NotNull SelectSelectStep<Record6<Long, String, String, LocalDateTime, Long, Long>> selectUploadProgress() {
        return PostgresDSL.select(
                UPLOAD_PROGRESS_INFO.ID,
                UPLOAD_PROGRESS_INFO.S3_UPLOAD_ID,
                UPLOAD_PROGRESS_INFO.KEY_NAME,
                UPLOAD_PROGRESS_INFO.EXPIRE_TS,
                UPLOAD_PROGRESS_INFO.SIZE,
                UPLOAD_PROGRESS_INFO.UPLOADER_USER_ID
        );
    }
}
