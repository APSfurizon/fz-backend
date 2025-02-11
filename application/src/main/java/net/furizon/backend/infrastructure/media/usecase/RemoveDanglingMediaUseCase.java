package net.furizon.backend.infrastructure.media.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.infrastructure.configuration.StorageConfig;
import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.backend.infrastructure.media.action.DeleteMediaAction;
import net.furizon.backend.infrastructure.media.action.PhysicallyDeleteMediaAction;
import net.furizon.backend.infrastructure.media.finder.MediaFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

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
            if (media.getStoreMethod() == StoreMethod.DISK) {
                if (!Files.exists(Paths.get(basePath, media.getPath()))) {
                    log.info("[DANGLING MEDIA] Deleting media without local file: {}", media);
                    dbDeleteIds.add(media.getId());
                    deleted++;
                }
                continue;
            }
        }

        //Find media on disk which are not present in the db and deletes them
        //Build set of every path
        Set<String> allFiles = new HashSet<>();
        medias.forEach(media -> {
            if (media.getStoreMethod() == StoreMethod.DISK) {
                allFiles.add(media.getPath());
            }
        });
        //Get paths
        int basePathLength = basePath.length();
        Path mediaPath = Paths.get(storageConfig.getFullMediaPath());
        //For each file
        try (Stream<Path> files = Files.find(mediaPath, Integer.MAX_VALUE, (path, attr) -> attr.isRegularFile())) {
            files.forEach(path -> {
                //If in the DB we don't have a file with the given normalized path. We also remove the base path
                if (!allFiles.contains(path.normalize().toString().substring(basePathLength))) {
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

        log.debug("[DANGLING MEDIA] Removing from db ids: {}", dbDeleteIds);
        deleteMediaAction.deleteFromDb(dbDeleteIds.stream().toList());

        return deleted;
    }
}
