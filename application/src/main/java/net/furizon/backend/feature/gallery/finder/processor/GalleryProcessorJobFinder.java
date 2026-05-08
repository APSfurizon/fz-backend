package net.furizon.backend.feature.gallery.finder.processor;

import net.furizon.backend.feature.gallery.dto.processor.GalleryProcessorJob;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface GalleryProcessorJobFinder {
    @NotNull Optional<GalleryProcessorJob> getJobByReqId(long reqId);
}
