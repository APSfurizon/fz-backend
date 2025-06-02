package net.furizon.backend.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixBalanceForProviderFinder;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneralChecks {
    @NotNull private final PretixBalanceForProviderFinder pretixBalanceForProviderFinder;
    @NotNull private final PermissionFinder permissionFinder;
    @NotNull private final OrderFinder orderFinder;

    public long getUserIdAssertPermissionCheckTimeframe(@Nullable Long userId,
                                                        @NotNull FurizonUser user,
                                                        @NotNull Permission permission,
                                                        @Nullable OffsetDateTime date,
                                                        @Nullable Event event) {
        boolean isAdmin = permissionFinder.userHasPermission(user.getUserId(), permission);
        assertTimeframeForEventNotPassedAllowAdmin(date, event, userId, user.getUserId(), null, isAdmin);
        return getUserIdAndAssertPermission(userId, user, permission, isAdmin);
    }

    public long getUserIdAndAssertPermission(@Nullable Long userId,
                                             @NotNull FurizonUser user,
                                             @NotNull Permission permission) {
        return getUserIdAndAssertPermission(userId, user, permission, null);
    }
    public long getUserIdAndAssertPermission(@Nullable Long userId,
                                             @NotNull FurizonUser user,
                                             @Nullable Permission permission,
                                             @Nullable Boolean isAdminCached) {
        long id = userId == null ? user.getUserId() : userId;
        if (userId != null && userId != user.getUserId()) {
            if (isAdminCached == null) {
                isAdminCached = permissionFinder.userHasPermission(
                        user.getUserId(),
                        Objects.requireNonNull(permission)
                );
            }
            if (!isAdminCached) {
                log.error("User {} has no permission over userId {}", user.getUserId(), userId);
                throw new ApiException("User is not an admin", GeneralResponseCodes.USER_IS_NOT_ADMIN);
            }
        }
        return id;
    }

    public boolean isTimeframeForEventOk(@Nullable OffsetDateTime date, @Nullable Event event) {
        if (date == null) {
            return true;
        }
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime eventEnd = event == null ? null : event.getDateTo();
        if (now.isAfter(date) && (eventEnd == null || now.isBefore(eventEnd))) {
            return false;
        }
        return true;
    }
    public void assertTimeframeForEventNotPassedAllowAdmin(@Nullable OffsetDateTime date,
                                                           @Nullable Event event,
                                                           @Nullable Long id,
                                                           long userId,
                                                           @Nullable Permission permission,
                                                           @Nullable Boolean isAdminCached) {
        if (id != null) {
            if (isAdminCached == null) {
                isAdminCached = permissionFinder.userHasPermission(userId, Objects.requireNonNull(permission));
            }
            if (isAdminCached) {
                return;
            }
        }
        assertTimeframeForEventNotPassed(date, event);

    }
    public void assertTimeframeForEventNotPassed(@Nullable OffsetDateTime date, @Nullable Event event) {
        if (!isTimeframeForEventOk(date, event)) {
            throw new ApiException("Editing timeframe has ended", GeneralResponseCodes.EDIT_TIMEFRAME_ENDED);
        }
    }


    @NotNull public Order getOrderAndAssertItExists(long userId, @NotNull Event event,
                                                    @NotNull PretixInformation pretixInformation) {
        Order order = orderFinder.findOrderByUserIdEvent(userId, event, pretixInformation);
        assertOrderFound(Optional.ofNullable(order), userId, event);
        return order;
    }
    public void assertUserHasOrder(long userId, @NotNull Event event) {
        Optional<Boolean> isDaily = orderFinder.isOrderDaily(userId, event);
        assertOrderFound(isDaily, userId, event);
    }
    public void assertUserHasOrderAndItsNotDaily(long userId, @NotNull Event event) {
        Optional<Boolean> isDaily = orderFinder.isOrderDaily(userId, event);
        assertOrderFound(isDaily, userId, event);
        assertOrderIsNotDailyPrint(isDaily.get(), userId, event);
    }
    public void assertOrderIsNotDaily(@NotNull Order order, long userId, @NotNull Event event) {
        assertOrderIsNotDailyPrint(order.isDaily(), userId, event);
    }
    public void assertUserHasNotAnOrder(long userId, @NotNull Event event) {
        var r = orderFinder.isOrderDaily(userId, event);
        if (r.isPresent()) {
            log.error("User {} has bought an order for event {}!", userId, event);
            throw new ApiException("User has already bought an order", GeneralResponseCodes.ORDER_ALREADY_BOUGHT);
        }
    }

    public void assertOrderIsPaid(long userId, @NotNull Event event) {
        var r = orderFinder.getOrderStatus(userId, event);
        assertOrderFound(r, userId, event);
        assertOrderStatusPaid(r.get(), userId, event);
    }
    public void assertOrderIsPaid(@NotNull Order order, long userId, @NotNull Event event) {
        assertOrderStatusPaid(order.getOrderStatus(), userId, event);
    }

    public void assertPaymentAndRefundConfirmed(@NotNull String orderCode, @NotNull Event event) {
        pretixBalanceForProviderFinder.get(orderCode, event, true);
    }
    private void assertOrderStatusPaid(@NotNull OrderStatus status, long userId, @NotNull Event event) {
        if (status != OrderStatus.PAID) {
            log.error("Order for user {} on event {} is not paid", userId, event);
            throw new ApiException("Order is not paid", GeneralResponseCodes.ORDER_NOT_PAID);
        }
    }

    public void assertOrderIsNotDailyPrint(boolean isDaily, long userId, @NotNull Event event) {
        if (isDaily) {
            log.error("User {} is trying to manage a room on event {}, but has a daily ticket!", userId, event);
            throw new ApiException("User has a daily ticket", GeneralResponseCodes.USER_HAS_DAILY_TICKET);
        }
    }

    public void assertOrderFound(Optional<?> r, long userId, @NotNull Event event) {
        if (!r.isPresent()) {
            log.error("No order found for user {} on event {}", userId, event);
            throw new ApiException("Order not found", GeneralResponseCodes.ORDER_NOT_FOUND);
        }
    }
}
