package net.furizon.backend.feature.gallery.finder;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.gallery.action.uploadProgress.createUploadAction.JooqCreateUploadAction;
import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.feature.gallery.dto.GalleryUploadPreview;
import net.furizon.backend.feature.gallery.mapper.GalleryPreviewMapper;
import net.furizon.backend.feature.gallery.mapper.GalleryUploadMapper;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.jooq.generated.enums.UploadStatus;
import net.furizon.jooq.generated.enums.UploadType;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.*;

@Component
@RequiredArgsConstructor
public class JooqUploadFinder implements UploadFinder {
    @NotNull
    private final SqlQuery query;

    @Override
    public int countUserUploadsOnEvent(long userId, @NotNull Event event) {
        return query.count(
            PostgresDSL.select(UPLOADS.ID)
            .from(UPLOADS)
            .where(
                UPLOADS.PHOTOGRAPHER_USER_ID.eq(userId)
                .and(UPLOADS.EVENT_ID.eq(event.getId()))
            //.and(UPLOADS.STATUS.in(UploadStatus.APPROVED, UploadStatus.PENDING))
            )
        );
    }

    @Override
    public @Nullable Long getPhotographerUserId(long uploadId) {
        return query.fetchFirst(
            PostgresDSL.select(UPLOADS.PHOTOGRAPHER_USER_ID)
            .from(UPLOADS)
            .where(UPLOADS.ID.eq(uploadId))
        ).mapOrNull(r -> r.get(UPLOADS.PHOTOGRAPHER_USER_ID));
    }
    @Override
    public @Nullable Long getOriginalUploaderUserId(long uploadId) {
        return query.fetchFirst(
            PostgresDSL.select(UPLOADS.ORIGINAL_UPLOADER_USER_ID)
            .from(UPLOADS)
            .where(UPLOADS.ID.eq(uploadId))
        ).mapOrNull(r -> r.get(UPLOADS.ORIGINAL_UPLOADER_USER_ID));
    }

    private @Nullable Long getMediaIdFromUploadId(long uploadId, @NotNull TableField<Record, Long> field) {
        return query.fetchFirst(
            PostgresDSL.select(field)
            .from(UPLOADS)
            .where(UPLOADS.ID.eq(uploadId))
        ).mapOrNull(r -> r.get(field));
    }
    private @Nullable String getMediaFilenameFromUploadId(long uploadId, @NotNull TableField<Record, Long> field) {
        return query.fetchFirst(
            PostgresDSL.select(MEDIA.MEDIA_PATH)
            .from(MEDIA)
            .innerJoin(UPLOADS)
            .on(
                MEDIA.MEDIA_ID.eq(field)
                .and(UPLOADS.ID.eq(uploadId))
            )
        ).mapOrNull(r -> r.get(MEDIA.MEDIA_PATH));
    }
    @Override
    public @Nullable Long getMainMediaIdFromUploadId(long uploadId) {
        return getMediaIdFromUploadId(uploadId, UPLOADS.MEDIA_ID);
    }
    @Override
    public @Nullable String getMainMediaFilenameFromUploadId(long uploadId) {
        return getMediaFilenameFromUploadId(uploadId, UPLOADS.MEDIA_ID);
    }
    @Override
    public @Nullable Long getThumbnailMediaIdFromUploadId(long uploadId) {
        return getMediaIdFromUploadId(uploadId, UPLOADS.THUMBNAIL_MEDIA_ID);
    }
    @Override
    public @Nullable String getThumbnailMediaFilenameFromUploadId(long uploadId) {
        return getMediaFilenameFromUploadId(uploadId, UPLOADS.THUMBNAIL_MEDIA_ID);
    }
    @Override
    public @Nullable Long getRenderMediaIdFromUploadId(long uploadId) {
        return getMediaIdFromUploadId(uploadId, UPLOADS.RENDERED_MEDIA_ID);
    }
    @Override
    public @Nullable String getRenderMediaFilenameFromUploadId(long uploadId) {
        return getMediaFilenameFromUploadId(uploadId, UPLOADS.RENDERED_MEDIA_ID);
    }


    @Override
    public @Nullable Long getUploadIdByHash(@NotNull String hash) {
        return query.fetchFirst(
            PostgresDSL.select(UPLOADS.ID)
            .from(UPLOADS)
            .where(UPLOADS.HASH.eq(JooqCreateUploadAction.hashToUuid(hash)))
        ).mapOrNull(r -> r.get(UPLOADS.MEDIA_ID));
    }
    @Override
    public @Nullable Long getUploadIdByHashOnEvent(@NotNull String hash, long eventId) {
        return query.fetchFirst(
            PostgresDSL.select(UPLOADS.ID)
            .from(UPLOADS)
            .where(
                UPLOADS.HASH.eq(JooqCreateUploadAction.hashToUuid(hash))
                .and(UPLOADS.EVENT_ID.eq(eventId))
            )
        ).mapOrNull(r -> r.get(UPLOADS.ID));
    }


    @Override
    public @NotNull List<Long> getUnprocessedUploadIds() {
        return query.fetch(
            PostgresDSL.select(UPLOADS.ID)
            .from(UPLOADS)
            .where(UPLOADS.UPLOAD_TYPE.eq(UploadType.UNPROCESSED))
        ).stream().map(r -> r.get(UPLOADS.ID)).toList();
    }

    @Override
    public @Nullable GalleryUpload getUploadById(long uploadId) {
        FullUploadObjSelected q = selectFullUploadObj();
        return query.fetchFirst(
                q.query.where(UPLOADS.ID.eq(uploadId))
        ).mapOrNull(r -> GalleryUploadMapper.map(
                r,
                q.media,
                q.render,
                q.thumbnail,
                q.userPropic
        ));
    }

    @Override
    public @NotNull List<GalleryUploadPreview> listPreview(
            @Nullable Long photographerId,
            @Nullable Long eventId,
            @Nullable Long reqUserId,
            boolean isReqUserAnAdmin,
            long fromId,
            long limit
    ) {
        Condition condition = UPLOADS.ID.greaterThan(fromId);

        if (reqUserId == null) {
            condition = condition.and(UPLOADS.STATUS.eq(UploadStatus.APPROVED));
        } else {
            if (!isReqUserAnAdmin) {
                condition = condition.and(
                   UPLOADS.STATUS.eq(UploadStatus.APPROVED)
                   .or(UPLOADS.PHOTOGRAPHER_USER_ID.eq(reqUserId))
               );
            }
        }

        if (photographerId != null) {
            condition = condition.and(UPLOADS.PHOTOGRAPHER_USER_ID.eq(photographerId));
        }
        if (eventId != null) {
            condition = condition.and(UPLOADS.EVENT_ID.eq(eventId));
        }

        return query.fetch(
            selectPreviewUploadObj()
            .where(condition)
            .orderBy(UPLOADS.ID)
            .limit(limit)
        ).stream().map(GalleryPreviewMapper::map).toList();
    }

    @Override
    public @NotNull SelectOnConditionStep<?> selectPreviewUploadObj() {
        return PostgresDSL.select(
                UPLOADS.ID,
                UPLOADS.PHOTOGRAPHER_USER_ID,
                UPLOADS.UPLOAD_TIMESTAMP,
                UPLOADS.STATUS,
                UPLOADS.ORIGINAL_FILE_NAME,
                UPLOADS.UPLOAD_TYPE,
                UPLOADS.IS_SELECTED,
                UPLOADS.EVENT_ID,
                //USERS.USER_FURSONA_NAME,
                MEDIA.MEDIA_ID,
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_TYPE,
                MEDIA.MEDIA_STORE_METHOD
            )
            .from(UPLOADS)
            .leftJoin(MEDIA)
            .on(UPLOADS.THUMBNAIL_MEDIA_ID.eq(MEDIA.MEDIA_ID));
    }

    public record FullUploadObjSelected(
            SelectOnConditionStep<Record> query,
            Table<?> media,
            Table<?> render,
            Table<?> thumbnail,
            Table<?> userPropic
    ) {}

    @Override
    @NotNull
    public FullUploadObjSelected selectFullUploadObj() {
        Table<?> media = MEDIA.as("main_media");
        Table<?> render = MEDIA.as("render_media");
        Table<?> thumbnail = MEDIA.as("thumbnail_media");
        Table<?> userPropic = MEDIA.as("propic");
        var query = PostgresDSL.select(
                        UPLOADS.ID,
                        UPLOADS.ORIGINAL_UPLOADER_USER_ID,
                        UPLOADS.UPLOAD_TIMESTAMP,
                        UPLOADS.SHOT_TIMESTAMP,
                        UPLOADS.STATUS,
                        UPLOADS.ORIGINAL_FILE_NAME,
                        UPLOADS.FILE_SIZE,
                        UPLOADS.RESOLUTION_WIDTH,
                        UPLOADS.RESOLUTION_HEIGTH,
                        UPLOADS.UPLOAD_TYPE,
                        UPLOADS.IS_SELECTED,
                        UPLOADS.REPOST_PERMISSIONS,
                        USERS.USER_ID,
                        USERS.USER_FURSONA_NAME,
                        USERS.USER_LOCALE,
                        USERS.USER_LANGUAGE,
                        userPropic.field(MEDIA.MEDIA_ID),
                        userPropic.field(MEDIA.MEDIA_PATH),
                        userPropic.field(MEDIA.MEDIA_TYPE),
                        userPropic.field(MEDIA.MEDIA_STORE_METHOD),
                        EVENTS.ID,
                        EVENTS.EVENT_SLUG,
                        EVENTS.EVENT_DATE_TO,
                        EVENTS.EVENT_DATE_FROM,
                        EVENTS.EVENT_IS_CURRENT,
                        EVENTS.EVENT_PUBLIC_URL,
                        EVENTS.EVENT_NAMES_JSON,
                        EVENTS.EVENT_IS_LIVE,
                        EVENTS.EVENT_TEST_MODE_ENABLED,
                        EVENTS.EVENT_IS_PUBLIC,
                        EVENTS.EVENT_GEO_LAT,
                        EVENTS.EVENT_GEO_LON,
                        media.field(MEDIA.MEDIA_ID),
                        media.field(MEDIA.MEDIA_PATH),
                        media.field(MEDIA.MEDIA_TYPE),
                        media.field(MEDIA.MEDIA_STORE_METHOD),
                        thumbnail.field(MEDIA.MEDIA_ID),
                        thumbnail.field(MEDIA.MEDIA_PATH),
                        thumbnail.field(MEDIA.MEDIA_TYPE),
                        thumbnail.field(MEDIA.MEDIA_STORE_METHOD),
                        render.field(MEDIA.MEDIA_ID),
                        render.field(MEDIA.MEDIA_PATH),
                        render.field(MEDIA.MEDIA_TYPE),
                        render.field(MEDIA.MEDIA_STORE_METHOD),
                        UPLOAD_EXIF.CAMERA_MAKER,
                        UPLOAD_EXIF.CAMERA_MODEL,
                        UPLOAD_EXIF.LENS_MAKER,
                        UPLOAD_EXIF.LENS_MODEL,
                        UPLOAD_EXIF.FOCAL,
                        UPLOAD_EXIF.SHUTTER,
                        UPLOAD_EXIF.APERTURE,
                        UPLOAD_EXIF.ISO,
                        UPLOAD_VIDEO_DATA.VIDEO_CODEC,
                        UPLOAD_VIDEO_DATA.AUDIO_CODEC,
                        UPLOAD_VIDEO_DATA.AUDIO_FREQUENCY,
                        UPLOAD_VIDEO_DATA.DURATION,
                        UPLOAD_VIDEO_DATA.FRAMERATE
                )
                .from(UPLOADS)
                .innerJoin(USERS)
                .on(UPLOADS.PHOTOGRAPHER_USER_ID.eq(USERS.USER_ID))
                .innerJoin(EVENTS)
                .on(UPLOADS.EVENT_ID.eq(EVENTS.ID))
                .leftJoin(userPropic)
                .on(USERS.MEDIA_ID_PROPIC.eq(userPropic.field(MEDIA.MEDIA_ID)))
                .innerJoin(media)
                .on(UPLOADS.MEDIA_ID.eq(media.field(MEDIA.MEDIA_ID)))
                .leftJoin(thumbnail)
                .on(UPLOADS.THUMBNAIL_MEDIA_ID.eq(thumbnail.field(MEDIA.MEDIA_ID)))
                .leftJoin(render)
                .on(UPLOADS.RENDERED_MEDIA_ID.eq(render.field(MEDIA.MEDIA_ID)))
                .leftJoin(UPLOAD_EXIF)
                .on(UPLOAD_EXIF.UPLOAD_ID.eq(UPLOADS.ID))
                .leftJoin(UPLOAD_VIDEO_DATA)
                .on(UPLOAD_VIDEO_DATA.UPLOAD_ID.eq(UPLOADS.ID));
        return new FullUploadObjSelected(
                query,
                media,
                render,
                thumbnail,
                userPropic
        );
    }
}
