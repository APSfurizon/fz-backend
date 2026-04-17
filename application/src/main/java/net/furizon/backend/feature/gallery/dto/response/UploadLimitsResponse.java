package net.furizon.backend.feature.gallery.dto.response;

import lombok.Builder;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Builder
public class UploadLimitsResponse {
    @NotNull private final List<EventUploadNo> uploadableEvents;
    private boolean bannedFromUploading;
    @Nullable private final Long uploadMaxFileSize;
    @Nullable private final Integer maxUploadsNumberPerEvent;
    @NotNull List<String> allowedMimeTypes;
    @NotNull List<String> allowedFileExtensions;

    @Data
    public static class EventUploadNo {
        private final Event event;
        private final int uploadedCount;
    }
}
