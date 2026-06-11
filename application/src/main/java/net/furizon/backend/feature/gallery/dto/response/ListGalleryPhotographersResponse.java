package net.furizon.backend.feature.gallery.dto.response;

import lombok.Data;
import net.furizon.backend.feature.gallery.dto.GalleryPhotographer;

import java.util.List;

@Data
public class ListGalleryPhotographersResponse {
    private final List<GalleryPhotographer> photographers;
}
