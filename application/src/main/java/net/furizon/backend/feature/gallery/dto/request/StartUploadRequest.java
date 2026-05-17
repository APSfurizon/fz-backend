package net.furizon.backend.feature.gallery.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import net.furizon.backend.feature.gallery.GalleryConstant;
import org.jetbrains.annotations.Nullable;

@Data
public class StartUploadRequest {
    @NotEmpty
    @Pattern(regexp = GalleryConstant.FILE_NAME_REGEX)
    private String fileName;
    @NotNull
    private final Long fileSize;
    @NotNull
    private final Long eventId; //used for check permissions

    @Nullable
    private final Long userId; //optional for admins
}
