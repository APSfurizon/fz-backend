package net.furizon.backend.infrastructure.media.action;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.ImageWriter;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.configuration.StorageConfig;
import net.furizon.backend.infrastructure.media.ImageConfig;
import net.furizon.backend.infrastructure.media.SimpleImageMetadata;
import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.backend.infrastructure.security.FurizonUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
public class StoreMediaOnDiskActionImpl implements StoreMediaOnDiskAction {
    @NotNull private final ImageConfig imageConfig;
    @NotNull private final StorageConfig storageConfig;
    @NotNull private final AddMediaAction addMediaAction;

    @NotNull private final ImageWriter writer;


    public StoreMediaOnDiskActionImpl(
            @NotNull ImageConfig imageConfig,
            @NotNull StorageConfig storageConfig,
            @NotNull AddMediaAction addMediaAction
    ) {
        this.imageConfig = imageConfig;
        this.storageConfig = storageConfig;
        this.addMediaAction = addMediaAction;
        //Q z m explaination here: https://developers.google.com/speed/webp/docs/cwebp
        this.writer = new WebpWriter()
                .withoutAlpha()
                .withQ(imageConfig.getWebpQuality());
    }

    @Override
    public @NotNull StoreMediaOnDiskAction.Results invoke(
            @NotNull ImmutableImage image, @NotNull SimpleImageMetadata metadata,
            @NotNull FurizonUser user, @NotNull String basePath) throws IOException {

        log.info("Storing a media on disk for user {} on basePath {}", user.getUsername(), basePath);

        String filename = UUID.randomUUID() + ".webp";
        String userId = String.valueOf(user.getUserId());

        Path relativePath = Paths.get(storageConfig.getMediaPath(), basePath, userId);
        Path fullRelativePath = relativePath.resolve(filename);
        Path baseStoragePath = Paths.get(storageConfig.getFullMediaPath());
        Path absolutePath = baseStoragePath.resolve(relativePath);
        Path fullAbsolutePath = baseStoragePath.resolve(fullRelativePath);
        Files.createDirectories(absolutePath);

        //Important to normalize the path before!
        String relativePathStr = fullRelativePath.normalize().toString();
        long mediaId = addMediaAction.invoke(relativePathStr, metadata.getType(), StoreMethod.DISK);
        image.output(writer, fullAbsolutePath);

        log.info("Stored media on disk for user {}. Absolute path: {}", user.getUsername(), fullAbsolutePath);

        return new Results(relativePathStr, mediaId);
    }


}
