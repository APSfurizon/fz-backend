package net.furizon.backend.feature.gallery.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.GalleryChecks;
import net.furizon.backend.feature.gallery.dto.response.UploadLimitsResponse;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.infrastructure.configuration.GalleryConfig;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetUploadLimitsUseCase implements UseCase<GetUploadLimitsUseCase.Input, UploadLimitsResponse> {
    @NotNull
    private final EventFinder eventFinder;
    @NotNull
    private final UploadFinder uploadFinder;
    @NotNull
    private final PermissionFinder permissionFinder;
    @NotNull
    private final GalleryChecks galleryChecks;
    @NotNull
    private final GeneralChecks checks;
    @NotNull
    private final GalleryConfig config;


    @Override
    public @NotNull UploadLimitsResponse executor(@NotNull GetUploadLimitsUseCase.Input input) {
        long userId = checks.getUserIdAndAssertPermission(
                input.userId,
                input.user,
                Permission.UPLOADS_CAN_MANAGE_UPLOADS
        );
        log.info("User {} is asking upload limits of user {}", input.user.getUserId(), userId);

        //We need two permission queries sadly
        Set<Permission> permissions = permissionFinder.getUserPermissions(userId);

        long maxFileSizeCap = galleryChecks.getBiggestFileSize(
                permissions,
                galleryChecks.getUploadFileSizeMbArr(),
                galleryChecks.getUploadFileSizePermissionsArr()
        );

        Integer uploadPerEventLimit = permissions.contains(Permission.UPLOADS_UNLIMITED_NUMBER_UPLOADER)
                                    ? null
                                    : config.getMaxLimitedUploadsPerEvent();
        OffsetDateTime now = OffsetDateTime.now();
        List<Event> events = eventFinder.getAttendedEvents(userId);
        List<UploadLimitsResponse.EventUploadNo> uploadableEvents = events.stream()
                .filter(e -> {
                    OffsetDateTime start = e.getCorrectDateFrom();
                    return start == null ? true : now.isAfter(start);
                })
                //A bit inefficient making multiple queries, but I think it's fine.
                // The alternative is making a modified call like uploadFinder.getGalleryEvents(photographerId)
                .map(e -> new UploadLimitsResponse.EventUploadNo(e, uploadFinder.countUserUploadsOnEvent(userId, e)))
                .filter(e -> uploadPerEventLimit == null ? true : e.getUploadedCount() < uploadPerEventLimit)
                .toList();

        return UploadLimitsResponse.builder()
                .uploadableEvents(uploadableEvents)
                .bannedFromUploading(permissions.contains(Permission.UPLOADS_BANNED_FROM_UPLOADING))
                .uploadMaxFileSize(maxFileSizeCap == Long.MAX_VALUE ? null : maxFileSizeCap) //MUST be MAX_VALUE
                .maxUploadsNumberPerEvent(uploadPerEventLimit)
                .allowedMimeTypes(SUPPORTED_MIME_TYPES)
                .allowedFileExtensions(SUPPORTED_FILE_EXTENSIONS)
            .build();
    }

    //You can manually export this list by calling GET /job/get-supported-types of the gallery processor project
    public static final List<String> SUPPORTED_FILE_EXTENSIONS = List.of(
            "tiff",
            "f4v,",
            "nef",
            "tif",
            "flv",
            "avi",
            "mov",
            "raf",
            "jpeg",
            "m4b",
            "m4a",
            "jpe",
            "jpg",
            "orf",
            "cr2",
            "qt",
            "heic",
            "arw",
            "png",
            "m4p",
            "crw",
            "heif",
            "m4r",
            "webp",
            "m4v",
            "mp4"
    );
    public static final List<String> SUPPORTED_MIME_TYPES = List.of(
            "image/png",
            "image/jpeg",
            "image/x-canon-cr2",
            "image/webp",
            "image/x-olympus-orf",
            "video/x-flv",
            "image/tiff",
            "image/x-nikon-nef",
            "image/x-canon-crw",
            "image/x-fuji-raf",
            "image/heif",
            "video/quicktime",
            "image/x-sony-arw",
            "video/vnd.avi",
            "video/mp4"
    );

    public record Input(
            @Nullable Long userId,
            @NotNull FurizonUser user
    ) {}
}
