package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.gallery.dto.response.ListGalleryEventsResponse;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListGalleryEventsUseCase implements UseCase<ListGalleryEventsUseCase.Input, ListGalleryEventsResponse> {
    @NotNull
    private final PermissionFinder permissionFinder;
    @NotNull
    private final UploadFinder uploadFinder;

    @Override
    public @NotNull ListGalleryEventsResponse executor(@NotNull Input input) {
        boolean isAdmin = input.user == null ? false : permissionFinder.userHasPermission(
                input.user.getUserId(),
                Permission.UPLOADS_CAN_MANAGE_UPLOADS
        );
        return new ListGalleryEventsResponse(
            uploadFinder.getGalleryEvents(input.photographerId, isAdmin)
        );
    }

    public record Input(
            @Nullable Long photographerId,
            @Nullable FurizonUser user
    ) {}
}
