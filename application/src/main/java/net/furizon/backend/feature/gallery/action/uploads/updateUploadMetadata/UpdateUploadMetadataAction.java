package net.furizon.backend.feature.gallery.action.uploads.updateUploadMetadata;

import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UpdateUploadMetadataAction {
    boolean invoke(
            @NotNull GalleryProcessorJob job,
            @Nullable Long thumbnailMediaId,
            @Nullable Long renderedMediaId
    );
}
