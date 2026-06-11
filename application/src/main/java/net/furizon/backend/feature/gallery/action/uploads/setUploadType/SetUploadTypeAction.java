package net.furizon.backend.feature.gallery.action.uploads.setUploadType;

import net.furizon.jooq.generated.enums.UploadType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface SetUploadTypeAction {
    boolean invoke(long uploadId, @NotNull UploadType type);

    boolean invoke(Collection<Long> uploadIds, @NotNull UploadType type);
}
