package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.order.action.pushAnswer.PushPretixAnswerAction;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.feature.pretix.objects.order.usecase.UpdateOrderInDb;
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
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final FrontendConfig config;
    @NotNull private final UpdateOrderInDb updateOrderInDb;

    @Override
    public @NotNull RedirectView executor(@NotNull RegisterUserOrder.Input input) {
        PretixInformation pretixService = input.pretixService;
        FurizonUser user = input.user;
        if (user == null) {
            return new RedirectView(config.getLoginRedirectUrl(input.request.getRequestURL().toString()));
        }
        var e = pretixService.getCurrentEvent();
        if (!e.isPresent()) {
            //TODO error
            return null;
        }
        Event event = e.get();

        int ordersNo = orderFinder.countOrdersOfUserOnEvent(user.getUserId(), event);
        if (ordersNo > 0) {
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
                //TODO error unable to fetch order
                return null;
            }

            var o = updateOrderInDb.execute(pretixOrder.get(), event, pretixService);
            if (!o.isPresent()) {
                //TODO error unable to parse order
                return null;
            }
            order = o.get();
        }

        if (!order.getPretixOrderSecret().equals(input.secret)) {
            //TODO error secret doesnt match
            return null;
        }

        order.setOrderOwnerUserId(user.getUserId());
        boolean success = pushPretixAnswerAction.invoke(order, pretixService);

        if (!success) {
           //TODO error pushing answers not good
            return null;
        }

        return null; //TODO return to order homepage
    }

    public record Input(
            @Nullable FurizonUser user,
            @NotNull String code,
            @NotNull String secret,
            @NotNull HttpServletRequest request,
            @NotNull PretixInformation pretixService
    ) {}
}
