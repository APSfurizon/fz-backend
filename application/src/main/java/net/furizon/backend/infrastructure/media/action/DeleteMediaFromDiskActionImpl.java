package net.furizon.backend.infrastructure.media.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.dto.MediaData;
import net.furizon.backend.infrastructure.configuration.StorageConfig;
import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.backend.infrastructure.media.finder.MediaFinder;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteMediaFromDiskActionImpl implements DeleteMediaFromDiskAction {
    @NotNull private final MediaFinder mediaFinder;
    @NotNull private final DeleteMediaAction deleteMediaAction;
    @NotNull private final StorageConfig storageConfig;

    @Override
    @Transactional
    public boolean invoke(@NotNull Set<Long> ids, boolean deleteFromDb) throws IOException {
        return invoke(mediaFinder.findByIds(ids), deleteFromDb);
    }

    @Override
    @Transactional
    public boolean invoke(long id, boolean deleteFromDb) throws IOException {
        return invoke(Set.of(id), deleteFromDb);
    }

    @Override
    @Transactional
    public boolean invoke(@NotNull List<MediaData> medias, boolean deleteFromDb) throws IOException {
        Path basePath = Paths.get(storageConfig.getFullMediaPath());

        for (MediaData media : medias) {
            if (media.getStoreMethod() != StoreMethod.DISK) {
                log.error("Unable to delete non-disk media {}", media);
                continue;
            }
            log.info("Deleting media {}", media);
            Path p = basePath.resolve(media.getPath());
            Files.deleteIfExists(p);
        }

        return deleteFromDb ? deleteMediaAction.deleteFromDb(medias.stream().map(MediaData::getId).toList()) : false;
    }

    @Override
    @Transactional
    public boolean invoke(@NotNull MediaData media, boolean deleteFromDb) throws IOException {
        return invoke(List.of(media), deleteFromDb);
    }
}
