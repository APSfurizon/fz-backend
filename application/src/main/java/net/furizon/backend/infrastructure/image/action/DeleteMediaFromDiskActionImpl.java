package net.furizon.backend.infrastructure.image.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.dto.MediaData;
import net.furizon.backend.infrastructure.configuration.StorageConfig;
import net.furizon.backend.infrastructure.image.finder.MediaFinder;
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
    public boolean invoke(@NotNull Set<Long> ids) throws IOException {
        return invoke(mediaFinder.findByIds(ids));
    }

    @Override
    @Transactional
    public boolean invoke(long id) throws IOException {
        return invoke(Set.of(id));
    }

    @Override
    @Transactional
    public boolean invoke(@NotNull List<MediaData> medias) throws IOException {
        Path basePath = Paths.get(storageConfig.getBasePath());

        for (MediaData media : medias) {
            log.info("Deleting media {}", media);
            Path p = basePath.resolve(media.getRelativePath());
            Files.deleteIfExists(p);
        }

        return deleteMediaAction.invoke(medias.stream().map(MediaData::getId).toList());
    }

    @Override
    @Transactional
    public boolean invoke(@NotNull MediaData media) throws IOException {
        return invoke(List.of(media));
    }
}
