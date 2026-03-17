package net.furizon.backend.feature.gallery.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.gallery.action.uploadProgress.createUploadAction.JooqCreateUploadAction;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.jooq.generated.enums.UploadType;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.UPLOADS;

@Component
@RequiredArgsConstructor
public class JooqUploadFinder implements UploadFinder {
    @NotNull
    private final SqlQuery query;

    @Override
    public int countUserUploadsOnEvent(long userId, @NotNull Event event) {
        return query.count(
            PostgresDSL.select(UPLOADS.ID)
            .from(UPLOADS)
            .where(
                UPLOADS.PHOTOGRAPHER_USER_ID.eq(userId)
                .and(UPLOADS.EVENT_ID.eq(event.getId()))
            //.and(UPLOADS.STATUS.in(UploadStatus.APPROVED, UploadStatus.PENDING))
            )
        );
    }

    @Override
    public @Nullable Long getUploaderUserId(long uploadId) {
        return query.fetchFirst(
            PostgresDSL.select(UPLOADS.PHOTOGRAPHER_USER_ID)
            .from(UPLOADS)
            .where(UPLOADS.ID.eq(uploadId))
        ).mapOrNull(r -> r.get(UPLOADS.PHOTOGRAPHER_USER_ID));
    }
    @Override
    public @Nullable Long getOriginalUploaderUserId(long uploadId) {
        return query.fetchFirst(
            PostgresDSL.select(UPLOADS.ORIGINAL_UPLOADER_USER_ID)
            .from(UPLOADS)
            .where(UPLOADS.ID.eq(uploadId))
        ).mapOrNull(r -> r.get(UPLOADS.ORIGINAL_UPLOADER_USER_ID));
    }

    @Override
    public @Nullable Long getMainMediaIdFromUploadId(long uploadId) {
        return query.fetchFirst(
            PostgresDSL.select(UPLOADS.MEDIA_ID)
            .from(UPLOADS)
            .where(UPLOADS.ID.eq(uploadId))
        ).mapOrNull(r -> r.get(UPLOADS.MEDIA_ID));
    }
    @Override
    public @Nullable String getMainMediaFilenameFromUploadId(long uploadId) {
        return query.fetchFirst(
            PostgresDSL.select(MEDIA.MEDIA_PATH)
            .from(MEDIA)
            .innerJoin(UPLOADS)
            .on(
                MEDIA.MEDIA_ID.eq(UPLOADS.MEDIA_ID)
                .and(UPLOADS.ID.eq(uploadId))
            )
        ).mapOrNull(r -> r.get(MEDIA.MEDIA_PATH));
    }

    @Override
    public @Nullable Long getUploadIdByHash(@NotNull String hash) {
        return query.fetchFirst(
            PostgresDSL.select(UPLOADS.ID)
            .from(UPLOADS)
            .where(UPLOADS.HASH.eq(JooqCreateUploadAction.hashToUuid(hash)))
        ).mapOrNull(r -> r.get(UPLOADS.MEDIA_ID));
    }
    @Override
    public @Nullable Long getUploadIdByHashOnEvent(@NotNull String hash, long eventId) {
        return query.fetchFirst(
            PostgresDSL.select(UPLOADS.ID)
            .from(UPLOADS)
            .where(
                UPLOADS.HASH.eq(JooqCreateUploadAction.hashToUuid(hash))
                .and(UPLOADS.EVENT_ID.eq(eventId))
            )
        ).mapOrNull(r -> r.get(UPLOADS.ID));
    }


    @Override
    public @NotNull List<Long> getUnprocessedUploadIds() {
        return query.fetch(
            PostgresDSL.select(UPLOADS.ID)
            .from(UPLOADS)
            .where(UPLOADS.UPLOAD_TYPE.eq(UploadType.UNPROCESSED))
        ).stream().map(r -> r.get(UPLOADS.ID)).toList();
    }
}
