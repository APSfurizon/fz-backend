package net.furizon.backend.feature.gallery.action.uploads.updateUploadMetadata;

import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJob;
import org.jetbrains.annotations.NotNull;

public interface UpdateUploadMetadataAction {
    boolean invoke(@NotNull GalleryProcessorJob job);
}
