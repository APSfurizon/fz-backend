package net.furizon.backend.feature.gallery.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.furizon.jooq.generated.enums.UploadRepostPermissions;

import java.util.List;

@Data
public class CompleteUploadRequest {
    @NotNull private final Long uploadReqId;
    @NotNull private final String fileName;
    @NotNull private final Long eventId;
    @NotNull private final UploadRepostPermissions uploadRepostPermissions;

    @NotNull private final List<String> etags;
    @NotNull private final String sha1Hash;
}
