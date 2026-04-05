package net.furizon.backend.feature.gallery.dto.response;

import lombok.Data;
import net.furizon.backend.feature.gallery.dto.GalleryUploadPreview;

import java.util.List;

@Data
public class ListUploadsResponse {
    private final List<GalleryUploadPreview> results;
}
