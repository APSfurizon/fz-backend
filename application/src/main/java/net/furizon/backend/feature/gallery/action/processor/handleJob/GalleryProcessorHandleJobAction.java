package net.furizon.backend.feature.gallery.action.processor.handleJob;

import jakarta.validation.constraints.NotNull;
import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJob;

public interface GalleryProcessorHandleJobAction {
    boolean invoke(@NotNull GalleryProcessorJob job);
}
