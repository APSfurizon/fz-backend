package net.furizon.backend.feature.gallery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.feature.gallery.dto.UploadProgress;
import net.furizon.backend.feature.gallery.finder.UploadFinder;
import net.furizon.backend.feature.gallery.finder.UploadProgressFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.configuration.GalleryConfig;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.media.ImageCodes;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GalleryChecks {
    @NotNull private final UploadProgressFinder uploadProgressFinder;
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final UploadFinder uploadFinder;
    @NotNull private final GalleryConfig galleryConfig;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final TranslationService translationService;

    public void assertUploadFound(@Nullable Object obj) {
        if (obj == null) {
            log.error("Upload not found");
            throw new ApiException(
                translationService.error("gallery.not_found", GalleryErrorCodes.UPLOADS_NOT_FOUND)
            );
        }
    }

    @NotNull
    public GalleryUpload getUploadAndAssertItExists(long uploadId) {
        var res = uploadFinder.getUploadById(uploadId);
        assertUploadFound(res);
        return res;
    }

    @NotNull
    public UploadProgress getUploadProgressAndAssertItExists(long uploadReqId, long userId) {
        var res = uploadProgressFinder.getUploadProgressByReqIdUser(uploadReqId, userId);
        assertUploadFound(res);
        return res;
    }

    public void assertUploadEnabledOnEvent(@NotNull Event event) {
        assertUploadEnabledOnEvent(event, null);
    }
    public void assertUploadEnabledOnEvent(@NotNull Event event, @Nullable Boolean isAdminCached) {
        if (isAdminCached != null && isAdminCached) {
            return;
        }
        OffsetDateTime start = event.getCorrectDateFrom();
        if (start != null) {
            OffsetDateTime now = OffsetDateTime.now();
            boolean canUpload = now.isAfter(start);
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
        assertUserNotBannedFromGallery(userId, null);
    }
    public void assertUserNotBannedFromGallery(long userId, @Nullable Boolean isAdminCached) {
        if (isAdminCached != null && isAdminCached) {
            return;
        }
        boolean banned = permissionFinder.userHasPermission(userId, Permission.UPLOADS_BANNED_FROM_UPLOADING);
        if (banned) {
            log.error("User {} is banned from uploading", userId);
            throw new ApiException(
                    translationService.error("gallery.banned_from_uploading"),
                    GalleryErrorCodes.UPLOADS_BANNED_FROM_UPLOADING
            );
        }
    }

    public void assertUserNotReachedUploadNumberLimitAdmin(long reqUserId,
                                                           @Nullable Long userId,
                                                           @NotNull Event event) {
        int uploadsNo = uploadFinder.countUserUploadsOnEvent(reqUserId, event);
        if (userId != null && userId != reqUserId) {
            uploadsNo += uploadFinder.countUserUploadsOnEvent(userId, event);
        }
        assertUserNotReachedUploadNumberLimit(reqUserId, event, uploadsNo);
    }
    public void assertUserNotReachedUploadNumberLimit(long userId, @NotNull Event event) {
        //We don't need to count pending uploads since BY DATABASE CONSTRAINT only one is allowed per user
        int uploadsNo = uploadFinder.countUserUploadsOnEvent(userId, event);
        assertUserNotReachedUploadNumberLimit(userId, event, uploadsNo);
    }
    public void assertUserNotReachedUploadNumberLimit(long userId, @NotNull Event event, int uploadsNo) {
        final int limit = galleryConfig.getMaxLimitedUploadsPerEvent();
        if (uploadsNo > limit) {
            if (!permissionFinder.userHasPermission(userId, Permission.UPLOADS_UNLIMITED_NUMBER_UPLOADER)) {
                log.error("User {} has reached the maximum number of uploads ({}) on event {}", userId, limit, event);
                throw new ApiException(
                        translationService.error("gallery.upload.no_limit", limit),
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
        Long uploaderId = uploadFinder.getPhotographerUserId(uploadId);
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

    public long[] getUploadFileSizeMbArr() {
        final long[] size = {
            galleryConfig.getMaxLimitedUploadSize(),
            galleryConfig.getMaxLimitedBigUploadSize(),
            Long.MAX_VALUE
        };
        return size;
    }
    public Permission[] getUploadFileSizePermissionsArr() {
        final Permission[] permissions = {
            null,
            Permission.UPLOADS_BIG_FILE_SIZE_UPLOADER,
            Permission.UPLOADS_UNLIMITED_FILE_SIZE_UPLOADER
        };
        return permissions;
    }
    public long getBiggestFileSize(@NotNull Set<Permission> userPermissions, long[] sizes, Permission[] permissions) {
        long biggest = sizes[0];
        for (Permission p : userPermissions) {
            for (int i = 0; i < permissions.length; i++) {
                long size = sizes[i];
                if (p == permissions[i] && size > biggest) {
                    biggest = size;
                }
            }
        }
        return biggest;
    }

    public void assertUserCanUploadFileSize(long userId, long fileSize) {
        long[] sizes = getUploadFileSizeMbArr();
        Permission[] permissions = getUploadFileSizePermissionsArr();

        List<Permission> permissionsList = new ArrayList<>(permissions.length);
        for (int i = 0; i < sizes.length; i++) {
            Permission p = permissions[i];
            if (sizes[i] <= fileSize && p != null) {
                permissionsList.add(p);
            }
        }

        if (!permissionsList.isEmpty()) {
            Set<Permission> userPermissions = permissionFinder.getUserPermissions(userId);
            long biggest = getBiggestFileSize(userPermissions, sizes, permissions);

            //if at least one element is contained, it returns true
            boolean userHasAnyPermission = userPermissions.removeAll(permissionsList);
            if (!userHasAnyPermission) {
                log.error("User {} has uploaded a file to big ({}). Limit: {}",  userId, fileSize, biggest);
                throw new ApiException(
                        translationService.error(
                                "gallery.upload.too_big",
                                PretixGenericUtils.humanReadableByteCountBin(biggest)
                        ),
                        ImageCodes.IMAGE_SIZE_TOO_BIG
                );
            }
        }
    }

    public long fullUploadChecksAndGetUserId(@NotNull FurizonUser user,
                                             @Nullable Long userIdFromReqBody,
                                             @NotNull Event event,
                                             long fileSize) {
        boolean isAdmin = permissionFinder.userHasPermission(
                user.getUserId(),
                Permission.UPLOADS_CAN_MANAGE_UPLOADS
        );
        long userId = generalChecks.getUserIdAndAssertPermission(
                userIdFromReqBody,
                user,
                Permission.UPLOADS_CAN_MANAGE_UPLOADS,
                isAdmin
        );

        if (!isAdmin) {
            //Check for event upload enabled
            assertUploadEnabledOnEvent(event);
            //Check if user has order on event
            generalChecks.assertOrderIsPaid(userId, event);
            //Check ban status
            assertUserNotBannedFromGallery(userId);
        }
        //Check for upload limit reached
        assertUserNotReachedUploadNumberLimitAdmin(user.getUserId(), userIdFromReqBody, event);
        //Check for file size
        assertUserCanUploadFileSize(user.getUserId(), fileSize);
        return userId;
    }
}
