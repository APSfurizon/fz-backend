package net.furizon.backend.feature.gallery.mapper;

import jakarta.validation.constraints.NotNull;
import net.furizon.backend.feature.gallery.dto.UploadImageMetadata;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.UPLOAD_EXIF;

public class ImageMetadataMapper {
    @NotNull
    public static UploadImageMetadata map(@NotNull Record r) {
        return UploadImageMetadata.builder()
                .cameraMaker(r.get(UPLOAD_EXIF.CAMERA_MAKER))
                .cameraModel(r.get(UPLOAD_EXIF.CAMERA_MODEL))
                .lensMaker(r.get(UPLOAD_EXIF.LENS_MAKER))
                .lensModel(r.get(UPLOAD_EXIF.LENS_MODEL))
                .focal(r.get(UPLOAD_EXIF.FOCAL))
                .shutter(r.get(UPLOAD_EXIF.SHUTTER))
                .aperture(r.get(UPLOAD_EXIF.APERTURE))
                .iso(r.get(UPLOAD_EXIF.ISO))
            .build();
    }
}
