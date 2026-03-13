package net.furizon.backend.feature.gallery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.configuration.GalleryConfig;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.media.ImageCodes;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GalleryChecks {
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final UploadFinder uploadFinder;
    @NotNull private final GalleryConfig galleryConfig;
    @NotNull private final TranslationService translationService;

    private void assertUploadFound(@Nullable Object obj) {
        if (obj == null) {
            log.error("Upload not found");
            throw new ApiException(
                translationService.error("gallery.not_found", GalleryErrorCodes.UPLOADS_NOT_FOUND)
            );
        }
    }

    public void assertUploadEnabledOnEvent(@NotNull Event event) {
        OffsetDateTime start = event.getCorrectDateFrom();
        if (start != null) {
            LocalDateTime now = LocalDateTime.now();
            boolean canUpload = now.isAfter(start.toLocalDateTime());
            log.error("Cannot upload on event {} before {}",  event, start);
            if (!canUpload) {
                throw new ApiException(
                        translationService.error("gallery.too_early_to_upload"),
                        GalleryErrorCodes.UPLOADS_TOO_EARLY_TO_UPLOAD
                );
            }
        }
    }

    public void assertUserNotBannedFromGallery(long userId) {
        boolean banned = permissionFinder.userHasPermission(userId, Permission.UPLOADS_BANNED_FROM_UPLOADING);
        if (banned) {
            log.error("User {} is banned from uploading", userId);
            throw new ApiException(
                    translationService.error("gallery.banned_from_uploading"),
                    GalleryErrorCodes.UPLOADS_BANNED_FROM_UPLOADING
            );
        }
    }

    public void assertUserNotReachedUploadNumberLimit(long userId, @NotNull Event event) {
        final int limit = galleryConfig.getMaxLimitedUploadsPerEvent();
        int uploadsNo = uploadFinder.countUserUploadsOnEvent(userId, event);
        if (uploadsNo > limit) {
            if (!permissionFinder.userHasPermission(userId, Permission.UPLOADS_UNLIMITED_NUMBER_UPLOADER)) {
                log.error("User {} has reached the maximum number of uploads ({}) on event {}", userId, limit, event);
                throw new ApiException(
                        translationService.error("gallery.upload_no_limit", limit),
                        GalleryErrorCodes.UPLOADS_TOO_MANY_UPLOADS
                );
            }
        }
    }

    public void assertUserCanManageUploads(long userId) {
        boolean admin = permissionFinder.userHasPermission(userId, Permission.UPLOADS_CAN_MANAGE_UPLOADS);
        if (!admin) {
            log.error("User {} has not can manage uploads", userId);
            throw new ApiException(
                    translationService.error("gallery.cannot_manage"),
                    GeneralResponseCodes.USER_IS_NOT_ADMIN
            );
        }
    }
    public void assertUserCanDeleteUpload(long userId, long uploadId) {
        Long uploaderId = uploadFinder.getUploaderUserId(uploadId);
        assertUploadFound(uploaderId);
        if (uploaderId != userId) {
            boolean admin = permissionFinder.userHasPermission(userId, Permission.UPLOADS_CAN_FULLY_DELETE_UPLOADS);
            if (!admin) {
                log.error("User {} has not can delete upload {}", userId, uploadId);
                throw new ApiException(
                        translationService.error("gallery.cannot_delete"),
                        GeneralResponseCodes.USER_IS_NOT_ADMIN
                );
            }
        }

    }

    public void assertUserCanUploadFileSize(long userId, long fileSize) {
        final long[] sizes = {
            galleryConfig.getMaxLimitedUploadSize(),
            galleryConfig.getMaxLimitedBigUploadSize(),
            Long.MAX_VALUE
        };
        final Permission[] permissions = {
            null,
            Permission.UPLOADS_BIG_FILE_SIZE_UPLOADER,
            Permission.UPLOADS_UNLIMITED_FILE_SIZE_UPLOADER
        };

        List<Permission> permissionsList = new ArrayList<>(permissions.length);
        for (int i = 0; i < sizes.length; i++) {
            Permission p = permissions[i];
            if (sizes[i] <= fileSize && p != null) {
                permissionsList.add(p);
            }
        }

        if (!permissionsList.isEmpty()) {
            Set<Permission> userPermissions = permissionFinder.getUserPermissions(userId);

            long biggest = sizes[0];
            for (Permission p : userPermissions) {
                for (int i = 0; i < permissions.length; i++) {
                    long size = sizes[i];
                    if (p == permissions[i] && size > biggest) {
                        biggest = size;
                    }
                }
            }

            //if at least one element is contained, it returns true
            boolean userHasAnyPermission = userPermissions.removeAll(permissionsList);
            if (!userHasAnyPermission) {
                log.error("User {} has uploaded a file to big ({}). Limit: {}",  userId, fileSize, biggest);
                throw new ApiException(
                        translationService.error(
                                "gallery.upload_too_big",
                                PretixGenericUtils.humanReadableByteCountBin(biggest)
                        ),
                        ImageCodes.IMAGE_SIZE_TOO_BIG
                );
            }
        }
    }
}
