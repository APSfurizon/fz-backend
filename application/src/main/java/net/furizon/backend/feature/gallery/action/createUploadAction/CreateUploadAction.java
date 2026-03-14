package net.furizon.backend.feature.gallery.action.createUploadAction;

import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.jooq.generated.enums.UploadRepostPermissions;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;

public interface CreateUploadAction {
    @Transactional
    GalleryUpload invoke(
            long uploaderUserId,
            long photographerUserId,
            @NotNull String fileName,
            long fileSize,
            @NotNull UploadRepostPermissions repostPermissions,
            @NotNull Event event,
            @NotNull String mediaPath,
            @NotNull String mediaMimeType,
            @NotNull StoreMethod mediaStoreMethod
    );
}
