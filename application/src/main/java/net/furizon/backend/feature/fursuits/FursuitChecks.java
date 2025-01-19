package net.furizon.backend.feature.fursuits;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.fursuits.dto.FursuitDisplayData;
import net.furizon.backend.feature.fursuits.finder.FursuitFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.infrastructure.fursuits.FursuitConfig;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FursuitChecks {
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final FursuitFinder fursuitFinder;
    @NotNull private final FursuitConfig fursuitConfig;


    public @NotNull FursuitDisplayData getFursuitAndAssertItExists(long fursuitId, @Nullable Event event) {
        FursuitDisplayData data = fursuitFinder.getFursuit(fursuitId, event);
        assertFursuitExists(data);
        return data;
    }
    public void assertFursuitExists(@Nullable FursuitDisplayData fursuit) {
        assertFursuitObjExists(fursuit);
    }

    public void assertUserHasPermissionOnFursuit(long userId, @NotNull FursuitDisplayData fursuit) {
        if (userId != fursuit.getOwnerId()
                && permissionFinder.userHasPermission(userId, Permission.CAN_MANAGE_USER_PUBLIC_INFO)) {
            log.error("User {} is trying to manage fursuit {} but it's not the owner!", userId, fursuit.getId());
            throw new ApiException("You cannot manage a fursuit which is not yours!",
                    GeneralResponseCodes.USER_IS_NOT_ADMIN);
        }
    }
    public void assertUserHasPermissionOnFursuit(long userId, long fursuitId) {
        Long fursuitOwnerId = fursuitFinder.getFursuitOwner(fursuitId);
        assertFursuitObjExists(fursuitOwnerId);
        if (userId != fursuitOwnerId
                && permissionFinder.userHasPermission(userId, Permission.CAN_MANAGE_USER_PUBLIC_INFO)) {
            log.error("User {} is trying to manage fursuit {} but it's not the owner!", userId, fursuitId);
            throw new ApiException("You cannot manage a fursuit which is not yours!",
                    GeneralResponseCodes.USER_IS_NOT_ADMIN);
        }
    }

    public void assertUserHasNotReachedMaxBackendFursuitNo(long userId) {
        //This suffers from race conditions
        int alreadyRegisteredSuits = fursuitFinder.countFursuitsOfUser(userId);
        if (alreadyRegisteredSuits >= fursuitConfig.getMaxBackendFursuitsNo()) {
            log.error("User {} has reached fursuit limits! Congrats!!!", userId);
            throw new ApiException("How many fursuits do you own wtf",
                    FursuitErrorCodes.TOO_MANY_FURSUITS_ARE_YOU_A_MILLIONAIRE);
        }
    }

    public void assertUserHasNotReachedMaxFursuitBadges(long userId, @NotNull Order order) {
        int max = fursuitConfig.getMaxExtraFursuits() + order.getExtraFursuits();
        int alreadyRegisteredSuits = fursuitFinder.countFursuitOfUserToEvent(userId, order);
        if (alreadyRegisteredSuits >= Math.min(max, (int) fursuitConfig.getMaxExtraFursuits())) {
            log.error("User {} has reached max fursuit badges. Max = {}", userId, max);
            throw new ApiException("Too many fursuit to current event", FursuitErrorCodes.FURSUIT_BADGES_ENDED);
        }
    }

    public void assertFursuitNotAlreadyBroughtToCurrentEvent(long fursuitId, @NotNull Order order) {
        boolean b = fursuitFinder.isFursuitBroughtToEvent(fursuitId, order);
        if (b) {
            log.error("Fursuit {} is already brought to current event {}!", fursuitId, order.getEventId());
            throw new ApiException("Fursuit is already brought to current event!",
                    FursuitErrorCodes.FURSUIT_ALREADY_BROUGHT_TO_CURRENT_EVENT);
        }
    }

    public void assertFursuitIsBroughtToCurrentEvent(long fursuitId, @NotNull Order order) {
        boolean b = fursuitFinder.isFursuitBroughtToEvent(fursuitId, order);
        if (!b) {
            log.error("Fursuit {} is not brought to current event {}!", fursuitId, order.getEventId());
            throw new ApiException("Fursuit is already brought to current event!",
                    FursuitErrorCodes.FURSUIT_NOT_BROUGHT_TO_CURRENT_EVENT);
        }
    }

    private void assertFursuitObjExists(@Nullable Object object) {
        if (object == null) {
            log.error("Fursuit not found!");
            throw new ApiException("Fursuit not found", FursuitErrorCodes.FURSUIT_NOT_FOUND);
        }
    }
}
