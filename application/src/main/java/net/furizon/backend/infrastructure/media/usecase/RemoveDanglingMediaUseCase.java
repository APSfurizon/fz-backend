package net.furizon.backend.infrastructure.media.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.dto.MediaData;
import net.furizon.backend.infrastructure.media.action.DeleteMediaFromDiskAction;
import net.furizon.backend.infrastructure.media.finder.MediaFinder;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveDanglingMediaUseCase implements UseCase<Void, Long> {
    @NotNull private final MediaFinder mediaFinder;
    @NotNull private final DeleteMediaFromDiskAction deleteMediaFromDiskAction;

    @Override
    public @NotNull Long executor(@NotNull Void input) {
        List<MediaData> medias = new LinkedList<>(mediaFinder.findAll());
        Set<Long> referencedMediaIds = mediaFinder.getReferencedMediaIds();

        for (MediaData media : medias) {
            if (!referencedMediaIds.contains(media.getId())) {

                deleteMediaFromDiskAction
                continue;
            }
        }

        return 0L;
    }
}
