package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import jakarta.annotation.PostConstruct;
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

    private RedirectView successView;
    @PostConstruct
    private void init() {
        successView = new RedirectView(config.getOrderHomepageUrl());
    }

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
            return new RedirectView(config.getOrderHomepageUrl(OrderWorkflowErrorCode.SERVER_ERROR));
        }
        Event event = e.get();
        log.info("[PRETIX] User {} is trying to claim order {} with secret {}",
                user.getUserId(), input.code, input.secret);

        //Check if user already owns an order
        Order prevOrder = orderFinder.findOrderByUserIdEvent(user.getUserId(), event, pretixService);
        if (prevOrder != null) {
            //If the order the user owns is the same we're trying to claim, do nothing and return
            if (prevOrder.getCode().equals(input.code)) {
                log.debug("[PRETIX] User {} already owned order {}", user.getUserId(), input.code);
                return successView;
            }
            log.error("[PRETIX] Registration of order {} failed: User already owns an order!", input.code);
            return new RedirectView(config.getOrderHomepageUrl(OrderWorkflowErrorCode.ORDER_MULTIPLE_DONE));
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
                return new RedirectView(config.getOrderHomepageUrl(OrderWorkflowErrorCode.SERVER_ERROR));
            }

            var o = updateOrderInDb.execute(pretixOrder.get(), event, pretixService);
            if (!o.isPresent()) {
                log.error("[PRETIX] Registration of order {} failed: "
                        + "Order was not in the db and an error occurred in "
                        + "parsing or storing the newly fetched order", input.code);
                return new RedirectView(config.getOrderHomepageUrl(OrderWorkflowErrorCode.SERVER_ERROR));
            }
            order = o.get();
        }

        //Check if secret matches
        if (!order.getPretixOrderSecret().equals(input.secret)) {
            return new RedirectView(config.getOrderHomepageUrl(OrderWorkflowErrorCode.ORDER_SECRET_NOT_MATCH));
        }

        //If this order is already owned by someone who, for some reasons, owns more than one order we can reclaim it
        Long prevOwnerId = order.getOrderOwnerUserId();
        if (prevOwnerId != null && prevOwnerId > 0L) {
            int ordersNoPrevOwner = orderFinder.countOrdersOfUserOnEvent(prevOwnerId, event);
            if (ordersNoPrevOwner <= 1) {
                log.error("[PRETIX] Registration of order {} failed: The order was already owned by {}",
                        input.code, prevOwnerId);
                return new RedirectView(config.getOrderHomepageUrl(
                        OrderWorkflowErrorCode.ORDER_ALREADY_OWNED_BY_SOMEBODY_ELSE));
            }
            log.info("[PRETIX] Order {} was already owned by {} (who had multiple orders). Changing ownership to {}",
                    input.code, prevOwnerId, user.getUserId());
        }

        order.setOrderOwnerUserId(user.getUserId());
        boolean success = pushPretixAnswerAction.invoke(order, pretixService);

        if (!success) {
            log.error("[PRETIX] Registration of order {} failed: An error occurred while pushing answers to pretix!",
                    input.code);
            return new RedirectView(config.getOrderHomepageUrl(OrderWorkflowErrorCode.SERVER_ERROR));
        }

        updateOrderAction.invoke(order, pretixService);
        log.info("[PRETIX] Order {} was successfully claimed by {}!", input.code, user.getUserId());

        return successView;
    }

    public record Input(
            @Nullable FurizonUser user,
            @NotNull String code,
            @NotNull String secret,
            @NotNull HttpServletRequest request,
            @NotNull PretixInformation pretixService
    ) {}
}
