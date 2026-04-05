package net.furizon.backend.feature.gallery.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import net.furizon.jooq.generated.enums.UploadRepostPermissions;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class CompleteUploadRequest {
    @NotEmpty
    @Pattern(regexp = "^[\\p{L}\\p{N}\\p{M}_\\-'()\\[\\]. ]{2,63}$")
    private final String fileName;

    @NotNull private final Long uploadReqId;
    @NotNull private final Long fileSize;
    @NotNull private final Long eventId;
    @NotNull private final UploadRepostPermissions uploadRepostPermissions;

    @NotNull private final List<String> etags;
    @NotNull private final String md5Hash;

    @Nullable private final Long userId; //For admins
}
