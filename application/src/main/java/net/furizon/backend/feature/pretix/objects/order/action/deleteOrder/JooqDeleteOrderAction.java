package net.furizon.backend.feature.pretix.objects.order.action.deleteOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.action.deleteMembershipCard.DeleteMembershipCardAction;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Set;

import static net.furizon.jooq.generated.Tables.ORDERS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqDeleteOrderAction implements DeleteOrderAction {
    @NotNull private final DeleteMembershipCardAction deleteMembershipCardAction;
    @NotNull private final MembershipCardFinder cardFinder;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final SqlCommand command;

    @Override
    public void invoke(@NotNull final Order order) {
        this.invoke(order.getId());
    }

    @Override
    public void invoke(@NotNull final String code) {
        deleteCard(code);
        command.execute(
            PostgresDSL.deleteFrom(ORDERS)
            .where(ORDERS.ORDER_CODE.eq(code))
        );
    }

    @Override
    public void invoke(long orderId) {
        deleteCard(orderId);
        command.execute(
            PostgresDSL.deleteFrom(ORDERS)
            .where(ORDERS.ID.eq(orderId))
        );
    }

    @Override
    public void invokeWithCodes(@NotNull Set<String> orderCodes) {
        for (String code : orderCodes) {
            deleteCard(code);
        }

        command.execute(
            PostgresDSL.deleteFrom(ORDERS)
            .where(ORDERS.ORDER_CODE.in(orderCodes))
        );
    }
    @Override
    public void invokeWithIds(@NotNull Set<Long> orderIds) {
        for (long orderId : orderIds) {
            deleteCard(orderId);
        }

        command.execute(
            PostgresDSL.deleteFrom(ORDERS)
            .where(ORDERS.ID.in(orderIds))
        );
    }

    private void deleteCard(@NotNull String orderCode) {
        Long orderId = orderFinder.getOrderIdByCode(orderCode);
        if (orderId != null) {
            deleteCard(orderId);
        } else {
            log.error("Tried deleting card of order {}, but it wasn't found", orderCode);
        }
    }

    private void deleteCard(long orderId) {
        MembershipCard card = cardFinder.getMembershipCardByOrderId(orderId);
        if (card != null) {
            boolean res = deleteMembershipCardAction.invoke(card);
            if (!res) {
                log.error("Failed to delete membership card {}/{} while deleting order {}",
                    card.getCardId(), card.getIdInYear(), orderId);
            }
        }
    }
}
