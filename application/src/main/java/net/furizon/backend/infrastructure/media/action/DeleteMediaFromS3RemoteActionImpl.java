package net.furizon.backend.infrastructure.media.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.media.StoreMethod;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.infrastructure.media.finder.MediaFinder;
import net.furizon.backend.infrastructure.media.usecase.RemoveDanglingMediaUseCase;
import net.furizon.backend.infrastructure.s3.actions.deleteUpload.S3DeleteUpload;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteMediaFromS3RemoteActionImpl implements DeleteMediaFromS3RemoteAction {
    @NotNull private final MediaFinder mediaFinder;
    @NotNull private final S3DeleteUpload s3DeleteUpload;
    @NotNull private final DeleteMediaAction deleteMediaAction;

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
        try {
            RemoveDanglingMediaUseCase.mediaWriteMutexLockException();

            for (MediaData media : medias) {
                if (media.getStoreMethod() != StoreMethod.S3_REMOTE) {
                    log.error("Unable to delete non-s3_remote media {}", media);
                    continue;
                }
                log.info("Deleting media {}", media);
                s3DeleteUpload.delete(media.getPath());
            }

            return deleteFromDb ? deleteMediaAction.deleteFromDb(
                    medias.stream().map(MediaData::getId).toList()
            ) : false;
        } finally {
            RemoveDanglingMediaUseCase.mediaWriteMutexUnlock();
        }
    }

    @Override
    @Transactional
    public boolean invoke(@NotNull MediaData media, boolean deleteFromDb) throws IOException {
        return invoke(List.of(media), deleteFromDb);
    }
}
