package net.furizon.backend.feature.gallery.mapper;

import net.furizon.backend.feature.gallery.dto.GalleryUploadPreview;
import net.furizon.backend.infrastructure.media.mapper.MediaResponseMapper;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.UPLOADS;

public class GalleryPreviewMapper {
    public static GalleryUploadPreview map(@NotNull Record r) {
        return GalleryUploadPreview.builder()
                .id(r.get(UPLOADS.ID))
                .photographerUserId(r.get(UPLOADS.PHOTOGRAPHER_USER_ID))
                .uploadDate(r.get(UPLOADS.UPLOAD_TIMESTAMP))
                .status(r.get(UPLOADS.STATUS))
                .fileName(r.get(UPLOADS.ORIGINAL_FILE_NAME))
                .type(r.get(UPLOADS.UPLOAD_TYPE))
                .thumbnailMedia(MediaResponseMapper.mapOrNull(r))
                .isSelected(r.get(UPLOADS.IS_SELECTED))
                .eventId(r.get(UPLOADS.EVENT_ID))
            .build();
    }
}
