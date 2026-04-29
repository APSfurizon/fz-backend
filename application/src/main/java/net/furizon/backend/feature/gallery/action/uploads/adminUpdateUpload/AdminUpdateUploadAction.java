package net.furizon.backend.feature.gallery.action.uploads.adminUpdateUpload;

import net.furizon.jooq.generated.enums.UploadRepostPermissions;
import net.furizon.jooq.generated.enums.UploadStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AdminUpdateUploadAction {
    boolean invoke(
            List<Long> uploadIds,
            @Nullable UploadStatus status,
            @Nullable UploadRepostPermissions repostPermissions,
            @Nullable Long photographerId,
            @Nullable Long eventId
    );
}
