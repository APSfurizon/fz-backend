package net.furizon.backend.feature.gallery.dto;

import lombok.Data;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import org.jetbrains.annotations.NotNull;

@Data
public class GalleryPhotographer {
    @NotNull private final UserDisplayData user;
    private final boolean isOfficialPhotographer;
    private final int photoNumber;
}
