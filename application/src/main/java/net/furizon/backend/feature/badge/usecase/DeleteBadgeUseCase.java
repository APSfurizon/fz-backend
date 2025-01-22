package net.furizon.backend.feature.badge.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.BadgeType;
import net.furizon.backend.feature.fursuits.FursuitChecks;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.feature.badge.finder.BadgeFinder;
import net.furizon.backend.infrastructure.media.ImageCodes;
import net.furizon.backend.infrastructure.media.action.DeleteMediaFromDiskAction;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteBadgeUseCase implements UseCase<DeleteBadgeUseCase.Input, Boolean> {
    @NotNull private final DeleteMediaFromDiskAction deleteMediaFromDiskAction;
    @NotNull private final BadgeFinder badgeFinder;

    @NotNull private final FursuitChecks fursuitChecks;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        try {
            long userId = input.user.getUserId();
            if (input.type == BadgeType.BADGE_FURSUIT) {
                Objects.requireNonNull(input.fursuitId);
                fursuitChecks.assertUserHasPermissionOnFursuit(userId, input.fursuitId);
            }
            log.info("[BADGE] User {} is deleting a {} badge: FursuitVal = {}",
                    userId, input.type, input.fursuitId);

            MediaData media = switch (input.type) {
                case BadgeType.BADGE_FURSUIT -> badgeFinder.getMediaDataOfFursuitBadge(input.fursuitId);
                case BadgeType.BADGE_USER -> badgeFinder.getMediaDataOfUserBadge(userId);
            };

            if (media != null) {
                return deleteMediaFromDiskAction.invoke(media, true);
            } else {
                throw new ApiException("Badge not found!", ImageCodes.BADGE_NOT_FOUND);
            }
        } catch (IOException e) {
            log.error("Exception while deleting a badge", e);
            return false;
        }
    }

    public record Input(
            @NotNull FurizonUser user,
            @NotNull BadgeType type,
            @Nullable Long fursuitId
    ) {}
}
