package net.furizon.backend.feature.gallery.action.uploads.upsertImageMetadata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.UploadImageMetadata;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.UPLOAD_EXIF;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqUpsertImageMetadata implements UpsertImageMetadataAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public boolean invoke(long uploadId, @NotNull UploadImageMetadata imageMetadata) {
        return command.execute(
            PostgresDSL.insertInto(
                UPLOAD_EXIF,
                UPLOAD_EXIF.UPLOAD_ID,
                UPLOAD_EXIF.CAMERA_MAKER,
                UPLOAD_EXIF.CAMERA_MODEL,
                UPLOAD_EXIF.LENS_MAKER,
                UPLOAD_EXIF.LENS_MODEL,
                UPLOAD_EXIF.FOCAL,
                UPLOAD_EXIF.SHUTTER,
                UPLOAD_EXIF.APERTURE,
                UPLOAD_EXIF.ISO
            )
            .values(
                uploadId,
                imageMetadata.getCameraMaker(),
                imageMetadata.getCameraModel(),
                imageMetadata.getLensMaker(),
                imageMetadata.getLensModel(),
                imageMetadata.getFocal(),
                imageMetadata.getShutter(),
                imageMetadata.getAperture(),
                imageMetadata.getIso()
            )
            .onConflict(UPLOAD_EXIF.UPLOAD_ID)
            .doUpdate()
            .set(UPLOAD_EXIF.CAMERA_MAKER, imageMetadata.getCameraMaker())
            .set(UPLOAD_EXIF.CAMERA_MODEL, imageMetadata.getCameraModel())
            .set(UPLOAD_EXIF.LENS_MAKER, imageMetadata.getLensMaker())
            .set(UPLOAD_EXIF.LENS_MODEL, imageMetadata.getLensModel())
            .set(UPLOAD_EXIF.FOCAL, imageMetadata.getFocal())
            .set(UPLOAD_EXIF.SHUTTER, imageMetadata.getShutter())
            .set(UPLOAD_EXIF.APERTURE, imageMetadata.getAperture())
            .set(UPLOAD_EXIF.ISO, imageMetadata.getIso())
        ) > 0;
    }
}
