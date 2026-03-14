package net.furizon.backend.infrastructure.media.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.infrastructure.configuration.StorageConfig;
import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.backend.infrastructure.media.action.DeleteMediaAction;
import net.furizon.backend.infrastructure.media.action.PhysicallyDeleteMediaAction;
import net.furizon.backend.infrastructure.media.finder.MediaFinder;
import net.furizon.backend.infrastructure.s3.actions.deleteUpload.S3DeleteUpload;
import net.furizon.backend.infrastructure.s3.actions.listObjects.S3ListObjects;
import net.furizon.backend.infrastructure.s3.actions.objectExists.S3KeyExists;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveDanglingMediaUseCase implements UseCase<Integer, Long> {
    @NotNull private final PhysicallyDeleteMediaAction physicallyDeleteMediaAction;
    @NotNull private final DeleteMediaAction deleteMediaAction;
    @NotNull private final MediaFinder mediaFinder;
    @NotNull private final StorageConfig storageConfig;

    @NotNull private final S3KeyExists s3KeyExists;
    @NotNull private final S3ListObjects s3ListObjects;
    @NotNull private final S3DeleteUpload s3DeleteUpload;

    @Override
    public @NotNull Long executor(@NotNull Integer uselessInput) {
        List<MediaData> medias = new LinkedList<>(mediaFinder.findAll());
        Set<Long> referencedMediaIds = mediaFinder.getReferencedMediaIds();
        Set<Long> dbDeleteIds = new HashSet<>();
        String basePath = storageConfig.getBasePublicPath();

        long deleted = 0L;
        for (MediaData media : medias) {
            //Delete unreferenced media db objects
            if (!referencedMediaIds.contains(media.getId())) {
                log.info("[DANGLING MEDIA] Deleting unreferenced db media: {}", media);
                physicallyDeleteMediaAction.invoke(media, false);
                dbDeleteIds.add(media.getId());
                deleted++;
                continue;
            }

            //Delete media db object without local file
            StoreMethod storeMethod = media.getStoreMethod();
            if (storeMethod == StoreMethod.DISK) {
                if (!Files.exists(Paths.get(basePath, media.getPath()))) {
                    log.info("[DANGLING MEDIA] Deleting media without local file: {}", media);
                    dbDeleteIds.add(media.getId());
                    deleted++;
                }
                continue;
            }
            if (storeMethod == StoreMethod.S3_REMOTE) {
                if (!s3KeyExists.invoke(media.getPath())) {
                    log.info("[DANGLING MEDIA] Deleting media without s3 remote file: {}", media);
                    dbDeleteIds.add(media.getId());
                    deleted++;
                }
                continue;
            }
        }

        //Find stored media which are not present in the db and deletes them
        //Build set of every path
        Set<String> allMediaOnDiskPaths = new HashSet<>();
        Set<String> allMediaOnRemoteS3Keys = new HashSet<>();
        medias.forEach(media -> {
            StoreMethod storeMethod = media.getStoreMethod();
            switch (storeMethod) {
                case DISK -> allMediaOnDiskPaths.add(media.getPath());
                case S3_REMOTE -> allMediaOnRemoteS3Keys.add(media.getPath());
                default -> {
                }
            }
        });

        // DISK STORAGE
        //Get paths
        int basePathLength = basePath.length();
        Path mediaPath = Paths.get(storageConfig.getFullMediaPath());
        //For each file
        try (Stream<Path> files = Files.find(mediaPath, Integer.MAX_VALUE, (path, attr) -> attr.isRegularFile())) {
            files.forEach(path -> {
                //If in the DB we don't have a file with the given normalized path. We also remove the base path
                if (!allMediaOnDiskPaths.contains(path.normalize().toString().substring(basePathLength))) {
                    try {
                        log.info("[DANGLING MEDIA] Deleting file without db object: {}", path);
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        log.error("[DANGLING MEDIA] Error while deleting file {} not present in db: {}",
                                path, e.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            log.error("[DANGLING MEDIA] Error while listing files: {}", e.getMessage());
        }

        // S3 REMOTE STORAGE
        s3ListObjects.forEach(s3Object -> {
            String key = s3Object.key();
            if (!allMediaOnRemoteS3Keys.contains(key)) {
                try {
                    log.info("[DANGLING MEDIA] Deleting s3 remote object without db object: {}", key);
                    s3DeleteUpload.delete(key);
                } catch (S3Exception e) {
                    log.error("[DANGLING MEDIA] Error while deleting s3 remote object {} not present in db: {}",
                            key, e.getMessage());
                }
            }
        });

        log.debug("[DANGLING MEDIA] Removing from db ids: {}", dbDeleteIds);
        deleteMediaAction.deleteFromDb(dbDeleteIds.stream().toList());

        return deleted;
    }
}
