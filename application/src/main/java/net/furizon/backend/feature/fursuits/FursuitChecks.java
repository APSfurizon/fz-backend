package net.furizon.backend.feature.fursuits;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.dto.FursuitData;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.fursuits.FursuitConfig;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.security.GeneralChecks;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class FursuitChecks {
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final GeneralChecks generalChecks;
    @NotNull private final FursuitFinder fursuitFinder;
    @NotNull private final FursuitConfig fursuitConfig;
    @NotNull private final TranslationService translationService;


    public boolean isAdmin(long userId) {
        return permissionFinder.userHasPermission(userId, Permission.CAN_MANAGE_USER_PUBLIC_INFO);

    }

    public void assertPermissionAndTimeframe(long userId, long fursuitId,
                                             @Nullable Event event, @Nullable OffsetDateTime deadline) {
        assertPermissionAndTimeframe(
            userId,
            fursuitId,
            event,
            deadline,
            isAdmin(userId)
        );
    }
    public void assertPermissionAndTimeframe(long userId, long fursuitId,
                                             @Nullable Event event, @Nullable OffsetDateTime deadline,
                                             @Nullable Boolean isAdminCached) {
        generalChecks.assertTimeframeForEventNotPassedAllowAdmin(
                deadline,
                event,
                fursuitId,
                userId,
                null,
                isAdminCached
        );
        assertUserHasPermissionOnFursuit(userId, fursuitId, isAdminCached);
    }

    public @NotNull FursuitData getFursuitAndAssertItExists(long fursuitId,
                                                            @Nullable Event event, @Nullable Long userId,
                                                            boolean isOwner) {
        FursuitData data = fursuitFinder.getFursuit(fursuitId, event, userId, isOwner);
        assertFursuitExists(data);
        return data;
    }
    public void assertFursuitExists(@Nullable FursuitData fursuit) {
        assertFursuitObjExists(fursuit);
    }

    public void assertUserHasPermissionOnFursuit(long userId, @NotNull FursuitData fursuit) {
        if (userId != fursuit.getOwnerId()
                && permissionFinder.userHasPermission(userId, Permission.CAN_MANAGE_USER_PUBLIC_INFO)) {
            log.error("User {} is trying to manage fursuit {} but it's not the owner!",
                userId, fursuit.getFursuit().getId());
            throw new ApiException(translationService.error("badge.fursuit.fail.not_owner"),
                GeneralResponseCodes.USER_IS_NOT_ADMIN);
        }
    }
    public void assertUserHasPermissionOnFursuit(long userId, long fursuitId) {
        assertUserHasPermissionOnFursuit(userId, fursuitId, null);
    }
    public void assertUserHasPermissionOnFursuit(long userId, long fursuitId, @Nullable Boolean isAdminCached) {
        Long fursuitOwnerId = fursuitFinder.getFursuitOwner(fursuitId);
        assertFursuitObjExists(fursuitOwnerId);
        if (isAdminCached == null) {
            isAdminCached = permissionFinder.userHasPermission(userId, Permission.CAN_MANAGE_USER_PUBLIC_INFO);
        }
        if (userId != fursuitOwnerId && !isAdminCached) {
            log.error("User {} is trying to manage fursuit {} but it's not the owner!", userId, fursuitId);
            throw new ApiException(translationService.error("badge.fursuit.fail.not_owner"),
                GeneralResponseCodes.USER_IS_NOT_ADMIN);
        }
    }

    public void assertUserHasNotReachedMaxBackendFursuitNo(long userId) {
        //This suffers from race conditions
        int alreadyRegisteredSuits = fursuitFinder.countFursuitsOfUser(userId);
        if (alreadyRegisteredSuits >= fursuitConfig.getMaxBackendFursuitsNo()) {
            log.error("User {} has reached fursuit limits! Congrats!!!", userId);
            throw new ApiException(translationService.error(
                    "badge.fursuit.fail.limit_reached",
                    fursuitConfig.getMaxBackendFursuitsNo()
            ), FursuitErrorCodes.TOO_MANY_FURSUITS_ARE_YOU_A_MILLIONAIRE);
        }
    }

    public void assertUserHasNotReachedMaxFursuitBadges(long userId, @NotNull Order order) {
        int max = fursuitConfig.getDefaultFursuitsNo() + order.getExtraFursuits();
        int alreadyRegisteredSuits = fursuitFinder.countFursuitOfUserToEvent(userId, order);
        if (alreadyRegisteredSuits >= Math.min(max, (int) fursuitConfig.getMaxExtraFursuits())) {
            log.error("User {} has reached max fursuit badges. Max = {}", userId, max);
            throw new ApiException(translationService.error("badge.fursuit.fail.bring_to_event_limit_reached"),
                    FursuitErrorCodes.FURSUIT_BADGES_ENDED);
        }
    }

    public void assertFursuitNotAlreadyBroughtToCurrentEvent(long fursuitId, @NotNull Order order) {
        boolean b = fursuitFinder.isFursuitBroughtToEvent(fursuitId, order);
        if (b) {
            log.error("Fursuit {} is already brought to current event {}!", fursuitId, order.getEventId());
            throw new ApiException(translationService.error("badge.fursuit.fail.already_bringing_to_event"),
                FursuitErrorCodes.FURSUIT_ALREADY_BROUGHT_TO_CURRENT_EVENT);
        }
    }

    public void assertFursuitIsBroughtToCurrentEvent(long fursuitId, @NotNull Order order) {
        boolean b = fursuitFinder.isFursuitBroughtToEvent(fursuitId, order);
        if (!b) {
            log.error("Fursuit {} is not brought to current event {}!", fursuitId, order.getEventId());
            throw new ApiException(translationService.error("badge.fursuit.fail.not_bringing_to_event"),
                FursuitErrorCodes.FURSUIT_NOT_BROUGHT_TO_CURRENT_EVENT);
        }
    }

    private void assertFursuitObjExists(@Nullable Object object) {
        if (object == null) {
            log.error("Fursuit not found!");
            throw new ApiException(translationService.error("badge.fursuit.not_found"),
                FursuitErrorCodes.FURSUIT_NOT_FOUND);
        }
    }
}
