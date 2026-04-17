package net.furizon.backend.feature.gallery.mapper;

import net.furizon.backend.feature.gallery.dto.GalleryUpload;
import net.furizon.backend.feature.pretix.objects.event.mapper.JooqEventMapper;
import net.furizon.backend.feature.user.mapper.JooqUserDisplayMapper;
import net.furizon.backend.infrastructure.media.dto.MediaResponse;
import net.furizon.backend.infrastructure.media.mapper.MediaResponseMapper;
import net.furizon.jooq.generated.enums.UploadType;
import org.jooq.Record;
import org.jooq.Table;

import static net.furizon.jooq.generated.Tables.UPLOADS;

public class GalleryUploadMapper {

    public static GalleryUpload map(
            Record r,
            Table<?> media,
            Table<?> render,
            Table<?> thumbnail,
            Table<?> userPropic
    ) {
        MediaResponse downloadMedia = MediaResponseMapper.map(r, media);
        MediaResponse displayMedia = MediaResponseMapper.mapOrNull(r, render);
        MediaResponse thumbnailMedia = MediaResponseMapper.mapOrNull(r, thumbnail);

        UploadType type = r.get(UPLOADS.UPLOAD_TYPE);
        displayMedia = displayMedia == null ? (type == UploadType.IMAGE ? downloadMedia : displayMedia) : displayMedia;

        return GalleryUpload.builder()
                .id(r.get(UPLOADS.ID))
                .originalUploader(r.get(UPLOADS.ORIGINAL_UPLOADER_USER_ID))
                .photographer(JooqUserDisplayMapper.map(r, false, userPropic))
                .uploadDate(r.get(UPLOADS.UPLOAD_TIMESTAMP))
                .shotDate(r.get(UPLOADS.SHOT_TIMESTAMP))
                .status(r.get(UPLOADS.STATUS))
                .fileName(r.get(UPLOADS.ORIGINAL_FILE_NAME))
                .fileSize(r.get(UPLOADS.FILE_SIZE))
                .width(r.get(UPLOADS.RESOLUTION_WIDTH))
                .height(r.get(UPLOADS.RESOLUTION_HEIGTH))
                .type(type)
                .downloadMedia(downloadMedia)
                .displayMedia(displayMedia)
                .thumbnailMedia(thumbnailMedia)
                .isSelected(r.get(UPLOADS.IS_SELECTED))
                .repostPermissions(r.get(UPLOADS.REPOST_PERMISSIONS))
                .event(JooqEventMapper.map(r))
                .photoMetadata(ImageMetadataMapper.map(r))
                .videoMetadata(VideoMetadataMapper.map(r))
            .build();
    }
}
