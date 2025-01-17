package net.furizon.backend.infrastructure.media.action;

import com.sksamuel.scrimage.ImmutableImage;
import net.furizon.backend.infrastructure.media.SimpleImageMetadata;
import net.furizon.backend.infrastructure.security.FurizonUser;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface StoreMediaOnDiskAction {
    /**
     * Store the image on disk, on the user's path

     * @param image Image to store
     * @param metadata Metadata of the image
     * @param user User who have uploaded the image. The image will be saved in its path
     * @param basePath Basepath were to save the file
     * @return The relative path of the stored image from the base path
     */
    @NotNull StoreMediaOnDiskAction.Results invoke(
            @NotNull ImmutableImage image,
            @NotNull SimpleImageMetadata metadata,
            @NotNull FurizonUser user,
            @NotNull String basePath
    ) throws IOException;

    record Results(
            String relativePath,
            long mediaDbId
    ) {}
}
