package net.furizon.backend.infrastructure.image.action;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.ImageWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.configuration.StorageConfig;
import net.furizon.backend.infrastructure.image.SimpleImageMetadata;
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
@RequiredArgsConstructor
public class StoreMediaOnDiskActionImpl implements StoreMediaOnDiskAction {
    @NotNull private final StorageConfig storageConfig;
    @NotNull private final AddMediaAction addMediaAction;

    @Override
    public @NotNull StoreMediaOnDiskAction.Results invoke(
            @NotNull ImmutableImage image, @NotNull SimpleImageMetadata metadata,
            @NotNull FurizonUser user, @NotNull String basePath, @NotNull ImageWriter writer) throws IOException {

        log.info("Storing a media on disk for user {} on basePath {}", user.getUsername(), basePath);

        String filename = UUID.randomUUID() + ".jpg";
        String userId = String.valueOf(user.getUserId());

        Path relativePath = Paths.get(basePath, userId, filename);
        Path absolutePath = Paths.get(storageConfig.getBasePath()).resolve(relativePath);
        Files.createDirectories(absolutePath);

        image.output(writer, absolutePath);
        String relativePathStr = relativePath.toString();
        long mediaId = addMediaAction.invoke(relativePathStr, metadata.getType());

        log.info("Stored media on disk for user {}. Absolute path: {}", user.getUsername(), absolutePath);

        return new Results(relativePathStr, mediaId);
    }


}
