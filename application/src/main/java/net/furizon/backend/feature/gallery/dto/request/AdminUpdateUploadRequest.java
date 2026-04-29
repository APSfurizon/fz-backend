package net.furizon.backend.feature.gallery.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import net.furizon.jooq.generated.enums.UploadRepostPermissions;
import net.furizon.jooq.generated.enums.UploadStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class AdminUpdateUploadRequest {
    @NotNull private final List<Long> uploadIds;
    @Nullable private final UploadStatus newStatus;
    @Nullable private final UploadRepostPermissions newRepostPermissions;
    @Nullable @Positive private final Long newPhotographerUserId;
    @Nullable @Positive private final Long newEventId;
}
