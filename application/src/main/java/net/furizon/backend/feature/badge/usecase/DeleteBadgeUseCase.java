package net.furizon.backend.feature.badge.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.badge.BadgeType;
import net.furizon.backend.feature.fursuits.FursuitChecks;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.configuration.BadgeConfig;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.media.dto.MediaData;
import net.furizon.backend.feature.badge.finder.BadgeFinder;
import net.furizon.backend.infrastructure.media.ImageCodes;
import net.furizon.backend.infrastructure.media.action.DeleteMediaFromDiskAction;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
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
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final BadgeFinder badgeFinder;
    @NotNull private final BadgeConfig badgeConfig;
    @NotNull private final GeneralChecks generalChecks;

    @NotNull private final FursuitChecks fursuitChecks;
    @NotNull private final TranslationService translationService;

    @Override
    public @NotNull Boolean executor(@NotNull Input input) {
        try {
            long userId = input.userId;
            boolean isAdmin = permissionFinder.userHasPermission(userId, Permission.CAN_MANAGE_USER_PUBLIC_INFO);

            if (input.checkTimeframe) {
                generalChecks.assertTimeframeForEventNotPassedAllowAdmin(
                        badgeConfig.getEditingDeadline(),
                        input.event,
                        input.fursuitId,
                        userId,
                        null,
                        isAdmin
                );
            }

            if (input.type == BadgeType.BADGE_FURSUIT) {
                Objects.requireNonNull(input.fursuitId);
                fursuitChecks.assertUserHasPermissionOnFursuit(userId, input.fursuitId, isAdmin);
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
                throw new ApiException(translationService.error("badge.not_found"), ImageCodes.BADGE_NOT_FOUND);
            }
        } catch (IOException e) {
            log.error("Exception while deleting a badge", e);
            return false;
        }
    }

    public record Input(
            long userId,
            @NotNull BadgeType type,
            @Nullable Long fursuitId,
            @NotNull Event event,
            boolean checkTimeframe
    ) {}
}
