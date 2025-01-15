package net.furizon.backend.feature.badge.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.dto.MediaData;
import net.furizon.backend.feature.badge.finder.MediaFinder;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.image.action.DeleteMediaAction;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeleteUserBadgeUsecase implements UseCase<Set<Long>, Boolean> {
    private final BadgeConfig badgeConfig;
    private DeleteMediaAction deleteMediaAction;
    private MediaFinder mediaFinder;

    @Transactional
    @Override
    public Boolean executor(@NotNull Set<Long> ids) {
        // Retrieve media items
        Set<MediaData> medias =  mediaFinder.findByIds(ids);

        // TODO: Delete files physically

        // Delete media
        deleteMediaAction.invoke(ids);
        return true;
    }
}
