package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.order.action.pushAnswer.PushPretixAnswerAction;
import net.furizon.backend.feature.pretix.objects.order.action.updateOrder.UpdateOrderAction;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.feature.pretix.objects.order.usecase.UpdateOrderInDb;
import net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@Data
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterUserOrder implements UseCase<RegisterUserOrder.Input, RedirectView> {
    @NotNull private final PushPretixAnswerAction pushPretixAnswerAction;
    @NotNull private final PretixOrderFinder pretixOrderFinder;
    @NotNull private final UpdateOrderAction updateOrderAction;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final FrontendConfig config;
    @NotNull private final UpdateOrderInDb updateOrderInDb;

    @Override
    public @NotNull RedirectView executor(@NotNull RegisterUserOrder.Input input) {
        PretixInformation pretixService = input.pretixService;
        FurizonUser user = input.user;
        if (user == null) {
            log.error("[PRETIX] Registration of order {} failed: User is not logged in", input.code);
            return new RedirectView(config.getLoginRedirectUrl(input.request.getRequestURL().toString()));
        }
        var e = pretixService.getCurrentEvent();
        if (!e.isPresent()) {
            log.error("[PRETIX] Registration of order {} failed: Unable to fetch current event", input.code);
            //TODO error event not present
            return null;
        }
        Event event = e.get();
        log.info("[PRETIX] User {} is trying to claim order {} with secret {}",
                user.getUserId(), input.code, input.secret);

        int ordersNo = orderFinder.countOrdersOfUserOnEvent(user.getUserId(), event);
        if (ordersNo > 0) {
            log.error("[PRETIX] Registration of order {} failed: User already owns an order!", input.code);
            //TODO error order already exist!
            return null;
        }

        Order order = orderFinder.findOrderByCodeEvent(input.code, event, pretixService);
        if (order == null) {
            //If order is not in the db, try to fetch it
            var eventInfo = event.getOrganizerAndEventPair();
            Optional<PretixOrder> pretixOrder = pretixOrderFinder.fetchOrderByCode(
                    eventInfo.getOrganizer(),
                    eventInfo.getEvent(),
                    input.code
            );
            if (!pretixOrder.isPresent()) {
                log.error("[PRETIX] Registration of order {} failed: "
                        + "Order was not in the db and we were unable to fetch it from pretix", input.code);
                //TODO error unable to fetch order
                return null;
            }

            var o = updateOrderInDb.execute(pretixOrder.get(), event, pretixService);
            if (!o.isPresent()) {
                log.error("[PRETIX] Registration of order {} failed: "
                        + "Order was not in the db and an error occurred in "
                        + "parsing or storing the newly fetched order", input.code);
                //TODO error unable to parse order
                return null;
            }
            order = o.get();
        }

        if (!order.getPretixOrderSecret().equals(input.secret)) {
            //TODO error secret doesnt match
            return null;
        }

        //If this order is already owned by someone who, for some reasons, owns more than one order we can reclaim it
        Long prevOwnerId = order.getOrderOwnerUserId();
        if (prevOwnerId != null && prevOwnerId > 0L) {
            int ordersNoPrevOwner = orderFinder.countOrdersOfUserOnEvent(prevOwnerId, event);
            if (ordersNoPrevOwner <= 1) {
                //We don't need to check if the user is already the owner of this order,
                //since it's already checked in the ordersNo check
                log.error("[PRETIX] Registration of order {} failed: The order was already owned by {}",
                        input.code, prevOwnerId);
                //TODO error trying to steal an already claimed order!
                return null;
            }
            log.info("[PRETIX] Order {} was already owned by {} (who had multiple orders). Changing ownership to {}",
                    input.code, prevOwnerId, user.getUserId());
        }

        order.setOrderOwnerUserId(user.getUserId());
        boolean success = pushPretixAnswerAction.invoke(order, pretixService);

        if (!success) {
            log.error("[PRETIX] Registration of order {} failed: An error occurred while pushing answers to pretix!",
                    input.code);
            //TODO error pushing answers not good
            return new RedirectView(config.getOrderHomepageUrl(OrderWorkflowErrorCode.SERVER_ERROR));
        }

        updateOrderAction.invoke(order, pretixService);
        log.info("[PRETIX] Order {} was successfully claimed by {}!", input.code, user.getUserId());

        return new RedirectView(config.getOrderHomepageUrl());
    }

    public record Input(
            @Nullable FurizonUser user,
            @NotNull String code,
            @NotNull String secret,
            @NotNull HttpServletRequest request,
            @NotNull PretixInformation pretixService
    ) {}
}
