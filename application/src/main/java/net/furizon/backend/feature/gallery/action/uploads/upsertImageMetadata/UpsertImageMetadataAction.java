package net.furizon.backend.feature.gallery.action.uploads.upsertImageMetadata;

import net.furizon.backend.feature.gallery.dto.UploadImageMetadata;
import org.jetbrains.annotations.NotNull;

public interface UpsertImageMetadataAction {
    boolean invoke(long uploadId, @NotNull UploadImageMetadata imageMetadata);
}
