package net.furizon.backend.feature.gallery.mapper;

import jakarta.validation.constraints.NotNull;
import net.furizon.backend.feature.gallery.dto.UploadImageMetadata;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.UPLOAD_EXIF;

public class ImageMetadataMapper {
    @Nullable
    public static UploadImageMetadata map(@NotNull Record r) {
        String cameraMaker = r.get(UPLOAD_EXIF.CAMERA_MAKER);
        String cameraModel = r.get(UPLOAD_EXIF.CAMERA_MODEL);
        String lensMaker = r.get(UPLOAD_EXIF.LENS_MAKER);
        String lensModel = r.get(UPLOAD_EXIF.LENS_MODEL);
        String focal = r.get(UPLOAD_EXIF.FOCAL);
        String shutter = r.get(UPLOAD_EXIF.SHUTTER);
        String aperture = r.get(UPLOAD_EXIF.APERTURE);
        String iso = r.get(UPLOAD_EXIF.ISO);

        if (cameraMaker == null
                && cameraModel == null
                && lensMaker == null
                && lensModel == null
                && focal == null
                && shutter == null
                && aperture == null
                && iso == null) {
            return null;
        }

        return UploadImageMetadata.builder()
                .cameraMaker(cameraMaker)
                .cameraModel(cameraModel)
                .lensMaker(lensMaker)
                .lensModel(lensModel)
                .focal(focal)
                .shutter(shutter)
                .aperture(aperture)
                .iso(iso)
            .build();
    }
}
