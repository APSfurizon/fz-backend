package net.furizon.backend.feature.gallery.action.uploads.upsertVideoMetadata;

import net.furizon.backend.feature.gallery.dto.UploadVideoMetadata;
import org.jetbrains.annotations.NotNull;

public interface UpsertVideoMetadataAction {
    boolean invoke(long uploadId, @NotNull UploadVideoMetadata videoMetadata);
}
