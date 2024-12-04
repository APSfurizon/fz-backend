package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SanityCheck {
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final MembershipCardFinder membershipCardFinder;

    public @NotNull List<OrderWorkflowErrorCode> execute(@NotNull FurizonUser user,
                                                @NotNull PretixInformation pretixInformation) {
        List<OrderWorkflowErrorCode> errors = new LinkedList<>();

        var e = pretixInformation.getCurrentEvent();
        if (e.isPresent()) {
            Event event = e.get();

            int ordersNo = orderFinder.countOrdersOfUserOnEvent(user.getUserId(), event);
            int membershipNo = membershipCardFinder.countCardsPerUserPerEvent(user.getUserId(), event);

            execute(ordersNo, membershipNo, errors);
        } else {
            errors.add(OrderWorkflowErrorCode.SERVER_ERROR);
        }

        return errors;
    }

    public void execute(int ordersNo, int membershipNo, @NotNull List<OrderWorkflowErrorCode> errors) {
        // An user owns more than one order
        if (ordersNo > 1) {
            errors.add(OrderWorkflowErrorCode.ORDER_MULTIPLE_DONE);
        }
        // An user owns more than one membership order
        if (membershipNo > 1) {
            errors.add(OrderWorkflowErrorCode.MEMBERSHIP_MULTIPLE_DONE);
        }

        // An user has at least one order, but no membership card
        if (ordersNo > 0 && membershipNo == 0) {
            errors.add(OrderWorkflowErrorCode.MEMBERSHIP_MISSING);
        }
    }
}
