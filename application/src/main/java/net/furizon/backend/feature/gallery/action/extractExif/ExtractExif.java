package net.furizon.backend.feature.gallery.action.extractExif;

import com.drew.lang.annotations.NotNull;
import net.furizon.backend.feature.gallery.dto.GalleryProcessorUploadData;

public interface ExtractExif {
    void parseExif(@NotNull String path, @NotNull GalleryProcessorUploadData resultObj);
}
