package net.furizon.backend.feature.gallery.action.uploads.adminUpdateUpload;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.generated.enums.UploadRepostPermissions;
import net.furizon.jooq.generated.enums.UploadStatus;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.UpdateSetMoreStep;
import org.jooq.UpdateSetStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.UPLOADS;

@Component
@RequiredArgsConstructor
public class JooqAdminUpdateUpload implements AdminUpdateUploadAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public boolean invoke(
            List<Long> uploadIds,
            @Nullable UploadStatus status,
            @Nullable UploadRepostPermissions repostPermissions,
            @Nullable Long photographerId,
            @Nullable Long eventId
    ) {
        UpdateSetStep<?> q = PostgresDSL.update(UPLOADS);
        if (status != null) {
            q = q.set(UPLOADS.STATUS, status);
        }
        if (repostPermissions != null) {
            q = q.set(UPLOADS.REPOST_PERMISSIONS, repostPermissions);
        }
        if (photographerId != null) {
            q = q.set(UPLOADS.PHOTOGRAPHER_USER_ID, photographerId);
        }
        if (eventId != null) {
            q = q.set(UPLOADS.EVENT_ID, eventId);
        }

        //We assume that at least one of the previous is not null.
        // Caller should check for that
        UpdateSetMoreStep<?> qu = (UpdateSetMoreStep<?>) q;

        return command.execute(
            qu.where(UPLOADS.ID.in(uploadIds))
        ) == uploadIds.size();
    }
}
