package net.furizon.backend.feature.gallery.action.processor.submitJob;

import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJob;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface GalleryProcessorSubmitJobAction {
    @NotNull Optional<GalleryProcessorJob> invoke(long reqId, @NotNull String fileName);
}
