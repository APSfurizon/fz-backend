package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.gallery.dto.response.ListGalleryPhotographersResponse;
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
public class ListGalleryPhotographersUseCase implements
        UseCase<ListGalleryPhotographersUseCase.Input, ListGalleryPhotographersResponse> {
    @NotNull
    private final PermissionFinder permissionFinder;
    @NotNull
    private final UploadFinder uploadFinder;

    @Override
    public @NotNull ListGalleryPhotographersResponse executor(@NotNull ListGalleryPhotographersUseCase.Input input) {
        boolean isAdmin = input.user == null ? false : permissionFinder.userHasPermission(
                input.user.getUserId(),
                Permission.UPLOADS_CAN_MANAGE_UPLOADS
        );
        return new ListGalleryPhotographersResponse(
            uploadFinder.getGalleryPhotographers(input.eventId, isAdmin)
        );
    }

    public record Input(
            @Nullable Long eventId,
            @Nullable FurizonUser user
    ) {}
}
