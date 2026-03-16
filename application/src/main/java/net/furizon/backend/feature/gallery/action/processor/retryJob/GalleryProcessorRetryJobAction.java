package net.furizon.backend.feature.gallery.action.processor.retryJob;

import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJob;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface GalleryProcessorRetryJobAction {
    @NotNull Optional<GalleryProcessorJob> invoke(long reqId);
}
