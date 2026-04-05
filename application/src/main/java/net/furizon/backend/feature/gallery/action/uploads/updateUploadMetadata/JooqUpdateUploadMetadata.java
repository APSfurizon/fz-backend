package net.furizon.backend.feature.gallery.action.uploads.updateUploadMetadata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJob;
import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorUploadData;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.tables.Uploads.UPLOADS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqUpdateUploadMetadata implements UpdateUploadMetadataAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public boolean invoke(
            @NotNull GalleryProcessorJob job,
            @Nullable Long thumbnailMediaId,
            @Nullable Long renderedMediaId
    ) {
        GalleryProcessorUploadData data = job.getResult();
        if (data == null) {
            return false;
        }
        return command.execute(
            PostgresDSL.update(UPLOADS)
            .set(UPLOADS.UPLOAD_TYPE, job.getType())
            .set(UPLOADS.RESOLUTION_WIDTH, data.getResolutionWidth())
            .set(UPLOADS.RESOLUTION_HEIGTH, data.getResolutionHeight())
            .set(UPLOADS.SHOT_TIMESTAMP, data.getShotTimestamp())
            .set(UPLOADS.FILE_SIZE, data.getFileSize())
            .set(UPLOADS.THUMBNAIL_MEDIA_ID, thumbnailMediaId)
            .set(UPLOADS.RENDERED_MEDIA_ID, renderedMediaId)
            .where(UPLOADS.ID.eq(job.getId()))
        ) > 0;
    }
}
