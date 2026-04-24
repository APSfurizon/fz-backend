package net.furizon.backend.feature.gallery.finder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.gallery.action.uploadProgress.createUploadAction.JooqCreateUploadAction;
import net.furizon.backend.feature.gallery.dto.GalleryPhotographer;
import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.feature.gallery.dto.GalleryUploadPreview;
import net.furizon.backend.feature.gallery.dto.GalleryEvent;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDirectDownloadFile;
import net.furizon.backend.feature.gallery.dto.bulkDownload.BulkDownloadFile;
import net.furizon.backend.feature.gallery.mapper.BulkDirectDownloadFileMapper;
import net.furizon.backend.feature.gallery.mapper.BulkDownloadFileMapper;
import net.furizon.backend.feature.gallery.mapper.GalleryEventMapper;
import net.furizon.backend.feature.gallery.mapper.GalleryPhotographerMapper;
import net.furizon.backend.feature.gallery.mapper.GalleryPreviewMapper;
import net.furizon.backend.feature.gallery.mapper.GalleryUploadMapper;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.jooq.generated.enums.UploadStatus;
import net.furizon.jooq.generated.enums.UploadType;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.SelectOnConditionStep;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static net.furizon.jooq.generated.Tables.EVENTS;
import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.PERMISSION;
import static net.furizon.jooq.generated.Tables.UPLOADS;
import static net.furizon.jooq.generated.Tables.UPLOAD_EXIF;
import static net.furizon.jooq.generated.Tables.UPLOAD_VIDEO_DATA;
import static net.furizon.jooq.generated.Tables.USERS;
import static net.furizon.jooq.generated.Tables.USER_HAS_ROLE;

@Component
@RequiredArgsConstructor
public class JooqUploadFinder implements UploadFinder {
    @NotNull
    private final SqlQuery query;

    @NotNull
    private final ObjectMapper objectMapper;

    @NotNull
    private final TranslationService translationService;

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
    public @NotNull List<GalleryUploadPreview> adminBatchApprovalRetrival(
            long fromId,
            long limit,
            List<Pair<Long, Long>> reservedRanges
    ) {
        Condition condition = UPLOADS.ID.greaterOrEqual(fromId)
                              .and(UPLOADS.STATUS.eq(UploadStatus.PENDING));

        for (var range : reservedRanges) {
            condition = condition.and(
                UPLOADS.ID.lessThan(range.getLeft())
                .or(UPLOADS.ID.greaterThan(range.getRight()))
            );
        }

        Table<?> eventPhotographer = PostgresDSL.select(
                        UPLOADS.EVENT_ID,
                        UPLOADS.PHOTOGRAPHER_USER_ID
                )
                .from(UPLOADS)
                .where(condition)
                .orderBy(UPLOADS.ID)
                .limit(1)
                .asTable("eventPhotographer");

        return query.fetch(
            selectPreviewUploadObj()
            .where(
                condition
                .and(UPLOADS.EVENT_ID.eq(eventPhotographer.field(UPLOADS.EVENT_ID)))
                .and(UPLOADS.PHOTOGRAPHER_USER_ID.eq(eventPhotographer.field(UPLOADS.PHOTOGRAPHER_USER_ID)))
            )
            .orderBy(UPLOADS.ID)
            .limit(limit)
        ).stream().map(GalleryPreviewMapper::map).toList();
    }

    @Override
    public @NotNull List<GalleryUploadPreview> listPreview(
            @Nullable Long photographerId,
            @Nullable Long eventId,
            @Nullable UploadStatus uploadStatus,
            @Nullable Long reqUserId,
            boolean isReqUserAnAdmin,
            long fromId,
            long limit
    ) {
        Condition condition = UPLOADS.ID.lessThan(fromId);

        if (uploadStatus == null || !isReqUserAnAdmin) {
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
        } else {
            condition = condition.and(UPLOADS.STATUS.eq(uploadStatus));
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

    @Override
    public @Nullable GalleryEvent getGalleryEvent(long eventId, @Nullable Long photographerId) {
        var q = selectGalleryEventObj(photographerId);
        return query.fetchFirst(
                q.query
                .where(EVENTS.ID.eq(eventId))
            )
            .mapOrNull(r -> GalleryEventMapper.map(r, q.media, q.render, q.thumbnail, q.countField, q.countTable));
    }
    @Override
    public @NotNull List<GalleryEvent> getGalleryEvents(@Nullable Long photographerId) {
        var q = selectGalleryEventObj(photographerId);
        return query.fetch(
                q.query
                .orderBy(EVENTS.EVENT_DATE_FROM, EVENTS.EVENT_DATE_TO, EVENTS.ID)
            )
            .stream()
            .map(r -> GalleryEventMapper.map(r, q.media, q.render, q.thumbnail, q.countField, q.countTable))
            .toList();
    }

    public record GalleryEventObjSelected(
        SelectOnConditionStep<Record> query,
        Table<?> media,
        Table<?> render,
        Table<?> thumbnail,
        Field<Integer> countField,
        Table<Record2<Long, Integer>> countTable
    ) {}

    @Override
    public @NotNull GalleryEventObjSelected selectGalleryEventObj(@Nullable Long photographerId) {
        Table<?> media = MEDIA.as("main_media");
        Table<?> render = MEDIA.as("render_media");
        Table<?> thumbnail = MEDIA.as("thumbnail_media");

        var countField = PostgresDSL.field("countField", Integer.class);
        var countTable =
            PostgresDSL.select(
                UPLOADS.EVENT_ID,
                PostgresDSL.countDistinct(UPLOADS.ID).as(countField)
            )
            .from(UPLOADS)
            .where(
                (photographerId == null ? PostgresDSL.trueCondition() : UPLOADS.PHOTOGRAPHER_USER_ID.eq(photographerId))
                .and(UPLOADS.STATUS.eq(UploadStatus.APPROVED))
            )
            .groupBy(UPLOADS.EVENT_ID)
            .asTable("countTable");

        var q = PostgresDSL.select(
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
                    countTable.field(countField),
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
                    render.field(MEDIA.MEDIA_STORE_METHOD)
                )
                .from(EVENTS)
                .innerJoin(countTable)
                .on(EVENTS.ID.eq(countTable.field(UPLOADS.EVENT_ID)))
                .leftJoin(
                    UPLOADS
                    .innerJoin(media)
                    .on(
                        UPLOADS.MEDIA_ID.eq(media.field(MEDIA.MEDIA_ID))
                        .and(UPLOADS.IS_SELECTED.eq(PostgresDSL.trueCondition()))
                    )
                    .leftJoin(thumbnail)
                    .on(UPLOADS.THUMBNAIL_MEDIA_ID.eq(thumbnail.field(MEDIA.MEDIA_ID)))
                    .leftJoin(render)
                    .on(UPLOADS.RENDERED_MEDIA_ID.eq(render.field(MEDIA.MEDIA_ID)))
                )
                .on(UPLOADS.EVENT_ID.eq(EVENTS.ID));

        return new GalleryEventObjSelected(
                q,
                media,
                render,
                thumbnail,
                countField,
                countTable
        );
    }

    @Override
    public @Nullable GalleryPhotographer getGalleryPhotographer(long photographerId, @Nullable Long eventId) {
        var q = selectGalleryPhotographerObj(eventId);
        return query.fetchFirst(
                q.query
                .where(q.countTable.field(UPLOADS.PHOTOGRAPHER_USER_ID).eq(photographerId))
        )
        .mapOrNull(r -> GalleryPhotographerMapper.map(r, q.countField, q.countTable, q.officialPhotographer));
    }
    @Override
    public @NotNull List<GalleryPhotographer> getGalleryPhotographers(@Nullable Long eventId) {
        var q = selectGalleryPhotographerObj(eventId);
        return query.fetch(
                q.query
                .orderBy(
                    q.officialPhotographer.desc(),
                    q.countTable.field(q.countField).desc(),
                    USERS.USER_FURSONA_NAME,
                    USERS.USER_ID
                )
        )
        .stream()
        .map(r -> GalleryPhotographerMapper.map(r, q.countField, q.countTable, q.officialPhotographer))
        .toList();
    }


    public record GalleryPhotographerObjSelected(
            SelectOnConditionStep<?> query,
            Field<Integer> countField,
            Table<Record2<Long, Integer>> countTable,
            Field<Boolean> officialPhotographer
    ) {}

    @Override
    public GalleryPhotographerObjSelected selectGalleryPhotographerObj(@Nullable Long eventId) {
        var officialPhotographer = PostgresDSL.field("officialPhotographer", Boolean.class);
        var countField = PostgresDSL.field("countField", Integer.class);
        var countTable =
            PostgresDSL.select(
                UPLOADS.PHOTOGRAPHER_USER_ID,
                PostgresDSL.countDistinct(UPLOADS.ID).as(countField)
            )
            .from(UPLOADS)
            .where(
                (eventId == null ? PostgresDSL.trueCondition() : UPLOADS.EVENT_ID.eq(eventId))
                .and(UPLOADS.STATUS.eq(UploadStatus.APPROVED))
            )
            .groupBy(UPLOADS.PHOTOGRAPHER_USER_ID)
            .asTable("countTable");
        var q = PostgresDSL.select(
                    USERS.USER_ID,
                    USERS.USER_FURSONA_NAME,
                    USERS.USER_LOCALE,
                    USERS.USER_LANGUAGE,
                    MEDIA.MEDIA_ID,
                    MEDIA.MEDIA_PATH,
                    MEDIA.MEDIA_TYPE,
                    MEDIA.MEDIA_STORE_METHOD,
                    countTable.field(countField),
                    USER_HAS_ROLE.ROLE_ID.isNotNull().as(officialPhotographer)
                )
                .from(USERS)
                .innerJoin(countTable)
                .on(USERS.USER_ID.eq(countTable.field(UPLOADS.PHOTOGRAPHER_USER_ID)))
                .leftJoin(MEDIA)
                .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
                .leftJoin(
                    USER_HAS_ROLE
                    .innerJoin(PERMISSION)
                    .on(
                        PERMISSION.ROLE_ID.eq(USER_HAS_ROLE.ROLE_ID)
                        .and(PERMISSION.PERMISSION_VALUE.eq(Permission.UPLOADS_OFFICIAL_UPLOADER.getValue()))
                    )
                )
                .on(USER_HAS_ROLE.USER_ID.eq(USERS.USER_ID));
        return new GalleryPhotographerObjSelected(
                q,
                countField,
                countTable,
                officialPhotographer
        );
    }

    @Override
    public @Nullable Long getFirstPendingUpload(long startFrom) {
        Field<Long> firstPending = PostgresDSL.field("firstPending", Long.class);
        return query.fetchFirst(
            PostgresDSL.select(
                PostgresDSL.coalesce(
                    PostgresDSL.select(UPLOADS.ID)
                    .from(UPLOADS)
                    .where(
                        UPLOADS.STATUS.eq(UploadStatus.PENDING)
                        .and(UPLOADS.ID.greaterOrEqual(startFrom))
                    )
                    .orderBy(UPLOADS.ID.asc())
                    .limit(1),
                    PostgresDSL.select(UPLOADS.ID)
                    .from(UPLOADS)
                    .where(UPLOADS.ID.greaterOrEqual(startFrom))
                    .orderBy(UPLOADS.ID.desc())
                    .limit(1)
                ).as(firstPending)
            )
        ).mapOrNull(r -> r.get(firstPending));
    }

    @Override
    public int countPendingUploads() {
        return query.count(
            PostgresDSL.select(UPLOADS.ID)
            .from(UPLOADS)
            .where(UPLOADS.STATUS.eq(UploadStatus.PENDING))
        );
    }

    @Override
    public @NotNull List<BulkDownloadFile> getBulkDownloadableFiles(Set<Long> ids) {
        return query.fetch(
            PostgresDSL.select(
                MEDIA.MEDIA_PATH,
                UPLOADS.ORIGINAL_FILE_NAME,
                UPLOADS.UPLOAD_TIMESTAMP,
                UPLOADS.FILE_SIZE,
                EVENTS.EVENT_NAMES_JSON,
                EVENTS.ID,
                USERS.USER_FURSONA_NAME,
                USERS.USER_ID
            )
            .from(UPLOADS)
            .innerJoin(MEDIA)
            .on(UPLOADS.MEDIA_ID.eq(MEDIA.MEDIA_ID))
            .innerJoin(EVENTS)
            .on(UPLOADS.EVENT_ID.eq(EVENTS.ID))
            .innerJoin(USERS)
            .on(UPLOADS.PHOTOGRAPHER_USER_ID.eq(USERS.USER_ID))
            .where(
                UPLOADS.ID.in(ids)
                .and(UPLOADS.STATUS.eq(UploadStatus.APPROVED))
            )
        ).stream().map(r -> BulkDownloadFileMapper.map(r, objectMapper, translationService)).toList();
    }

    @Override
    public @NotNull List<BulkDirectDownloadFile> getBulkDirectDownloadableFiles(Set<Long> ids) {
        return query.fetch(
            PostgresDSL.select(
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_STORE_METHOD,
                UPLOADS.ORIGINAL_FILE_NAME,
                UPLOADS.UPLOAD_TIMESTAMP,
                UPLOADS.FILE_SIZE
            )
            .from(UPLOADS)
            .innerJoin(MEDIA)
            .on(UPLOADS.MEDIA_ID.eq(MEDIA.MEDIA_ID))
            .where(
                UPLOADS.ID.in(ids)
                .and(UPLOADS.STATUS.eq(UploadStatus.APPROVED))
            )
        ).stream().map(BulkDirectDownloadFileMapper::map).toList();
    }
}
