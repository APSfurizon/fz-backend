package net.furizon.backend.feature.gallery.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.tables.Uploads.UPLOADS;

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
        return query.fetchSingle(
            PostgresDSL.select(UPLOADS.PHOTOGRAPHER_USER_ID)
            .from(UPLOADS)
            .where(UPLOADS.ID.eq(uploadId))
        ).map(r -> r.get(UPLOADS.PHOTOGRAPHER_USER_ID));
    }
    @Override
    public @Nullable Long getOriginalUploaderUserId(long uploadId) {
        return query.fetchSingle(
            PostgresDSL.select(UPLOADS.ORIGINAL_UPLOADER_USER_ID)
            .from(UPLOADS)
            .where(UPLOADS.ID.eq(uploadId))
        ).map(r -> r.get(UPLOADS.ORIGINAL_UPLOADER_USER_ID));
    }
}
