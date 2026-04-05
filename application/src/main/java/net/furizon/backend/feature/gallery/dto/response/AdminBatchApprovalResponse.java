package net.furizon.backend.feature.gallery.dto.response;

import lombok.Data;
import net.furizon.backend.feature.gallery.dto.GalleryEvent;
import net.furizon.backend.feature.gallery.dto.GalleryUploadPreview;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class AdminBatchApprovalResponse {
    @NotNull private final List<GalleryUploadPreview> results;
    @Nullable private final UserDisplayData photographer;
    @Nullable private final GalleryEvent event;
    private final int stillPendingCount;
}
