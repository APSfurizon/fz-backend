package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.gallery.dto.GalleryUploadPreview;
import net.furizon.backend.feature.gallery.dto.response.ListUploadsResponse;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.infrastructure.configuration.GalleryConfig;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.jooq.generated.enums.UploadStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListUploadsUseCase implements UseCase<ListUploadsUseCase.Input, ListUploadsResponse> {
    @NotNull
    private final PermissionFinder permissionFinder;
    @NotNull
    private final UploadFinder uploadFinder;
    @NotNull
    private final GalleryConfig galleryConfig;

    @Override
    public @NotNull ListUploadsResponse executor(@NotNull ListUploadsUseCase.Input input) {

        Long userId = input.user == null ? null : input.user.getUserId();
        boolean isAdmin = userId == null
                ? false
                : permissionFinder.userHasPermission(userId, Permission.UPLOADS_CAN_MANAGE_UPLOADS);
        UploadStatus status = isAdmin ? input.uploadStatus : null;

        List<GalleryUploadPreview> resp = uploadFinder.listPreview(
            input.photographerId,
            input.eventId,
            status,
            userId,
            isAdmin,
            input.from == null ? Long.MAX_VALUE : input.from,
            galleryConfig.getListingBatchSize()
        );
        return new ListUploadsResponse(resp);
    }

    public record Input(
            @Nullable Long from,
            @Nullable Long photographerId,
            @Nullable Long eventId,
            @Nullable UploadStatus uploadStatus,
            @Nullable FurizonUser user
    ) {}
}
