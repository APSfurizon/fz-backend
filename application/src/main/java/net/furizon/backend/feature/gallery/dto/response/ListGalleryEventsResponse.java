package net.furizon.backend.feature.gallery.dto.response;

import lombok.Data;
import net.furizon.backend.feature.gallery.dto.GalleryEvent;

import java.util.List;

@Data
public class ListGalleryEventsResponse {
    private final List<GalleryEvent> events;
}
