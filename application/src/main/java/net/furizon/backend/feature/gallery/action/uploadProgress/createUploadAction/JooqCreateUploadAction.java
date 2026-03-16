package net.furizon.backend.feature.gallery.action.uploadProgress.createUploadAction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.backend.infrastructure.media.action.AddMediaAction;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.jooq.generated.enums.UploadRepostPermissions;
import net.furizon.jooq.generated.enums.UploadStatus;
import net.furizon.jooq.generated.enums.UploadType;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Objects;

import static net.furizon.jooq.generated.tables.Uploads.UPLOADS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqCreateUploadAction implements CreateUploadAction {
    @NotNull
    private final SqlCommand command;

    @NotNull
    private final AddMediaAction addMediaAction;

    @NotNull
    private final UserFinder userFinder;

    @Override
    @Transactional
    public GalleryUpload invoke(
            long uploaderUserId,
            long photographerUserId,
            @NotNull String fileName,
            long fileSize,
            @NotNull UploadRepostPermissions repostPermissions,
            @NotNull Event event,
            @NotNull String mediaPath,
            @NotNull String mediaMimeType,
            @NotNull StoreMethod mediaStoreMethod
    ) {

        long mediaId = addMediaAction.invoke(
                mediaPath,
                mediaMimeType,
                mediaStoreMethod
        );

        long uploadId = command.executeResult(
            PostgresDSL.insertInto(
                UPLOADS,
                UPLOADS.ORIGINAL_UPLOADER_USER_ID,
                UPLOADS.PHOTOGRAPHER_USER_ID,
                UPLOADS.ORIGINAL_FILE_NAME,
                UPLOADS.FILE_SIZE,
                UPLOADS.REPOST_PERMISSIONS,
                UPLOADS.EVENT_ID,
                UPLOADS.MEDIA_ID
            ).values(
                uploaderUserId,
                photographerUserId,
                fileName,
                fileSize,
                repostPermissions,
                event.getId(),
                mediaId
            )
            .returning(UPLOADS.ID)
        ).getFirst().map(record -> record.get(UPLOADS.ID));

        MediaResponse media = new MediaResponse(
                mediaId,
                MediaData.getFullPath(mediaStoreMethod, mediaPath),
                mediaMimeType
        );

        return GalleryUpload.builder()
                .id(uploadId)
                .photographer(Objects.requireNonNull(userFinder.getDisplayUser(photographerUserId, event)))
                .uploadDate(OffsetDateTime.now())
                .status(UploadStatus.PENDING) //TODO find a way to plug the actual default coming from the db
                .fileName(fileName)
                .fileSize(fileSize)
                .width(0)
                .height(0)
                .type(UploadType.UNPROCESSED)
                .media(media)
                .isSelected(false)
                .repostPermissions(repostPermissions)
                .event(event)
            .build();
    }
}
