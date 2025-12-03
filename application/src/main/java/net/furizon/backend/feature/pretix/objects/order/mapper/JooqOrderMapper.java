package net.furizon.backend.feature.pretix.objects.order.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixAnswer;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.pretix.model.Board;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.ORDERS;

@Component
@RequiredArgsConstructor
public class JooqOrderMapper {
    private final TypeReference<List<PretixAnswer>> pretixAnswerRef = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    private final EventFinder eventFinder;
    private final UserFinder userFinder;


    @NotNull
    public Order map(@NotNull final Record record, @NotNull final PretixInformation pretixInformation) {
        return map(record, pretixInformation, true);
    }

    @NotNull
    public Order map(@NotNull Record record, @NotNull PretixInformation pretixInformation, boolean loadAnswers) {
        try {
            Long userId = record.get(ORDERS.USER_ID);
            var v = Order.builder()
                    .code(record.get(ORDERS.ORDER_CODE))
                    .orderStatus(OrderStatus.values()[record.get(ORDERS.ORDER_STATUS)])
                    .sponsorship(Sponsorship.getFromDbId(record.get(ORDERS.ORDER_SPONSORSHIP_TYPE)))
                    .extraDays(ExtraDays.values()[record.get(ORDERS.ORDER_EXTRA_DAYS_TYPE)])
                    .dailyDays(record.get(ORDERS.ORDER_DAILY_DAYS))
                    .board(Board.getFromDbId(record.get(ORDERS.ORDER_BOARD)))
                    .pretixRoomItemId(record.get((ORDERS.ORDER_ROOM_PRETIX_ITEM_ID)))
                    .roomCapacity(record.get(ORDERS.ORDER_ROOM_CAPACITY))
                    .hotelInternalName(record.get(ORDERS.ORDER_HOTEL_INTERNAL_NAME))
                    .roomInternalName(record.get(ORDERS.ORDER_ROOM_INTERNAL_NAME))
                    .pretixOrderSecret(record.get(ORDERS.ORDER_SECRET))
                    .checkinSecret(record.get(ORDERS.ORDER_CHECKIN_SECRET))
                    .hasMembership(record.get(ORDERS.HAS_MEMBERSHIP))
                    .buyerEmail(record.get(ORDERS.ORDER_BUYER_EMAIL))
                    .buyerPhone(record.get(ORDERS.ORDER_BUYER_PHONE))
                    .buyerUser(record.get(ORDERS.ORDER_BUYER_USER))
                    .buyerLocale(record.get(ORDERS.ORDER_BUYER_LOCALE))
                    .ticketPositionId(record.get(ORDERS.ORDER_TICKET_POSITION_ID))
                    .ticketPositionPosid(record.get(ORDERS.ORDER_TICKET_POSITION_POSITIONID))
                    .roomPositionId(record.get(ORDERS.ORDER_ROOM_POSITION_ID))
                    .roomPositionPosid(record.get(ORDERS.ORDER_ROOM_POSITION_POSITIONID))
                    .earlyPositionId(record.get(ORDERS.ORDER_EARLY_POSITION_ID))
                    .latePositionId(record.get(ORDERS.ORDER_LATE_POSITION_ID))
                    .boardPositionId(record.get(ORDERS.ORDER_BOARD_POSITION_ID))
                    .extraFursuits(record.get(ORDERS.ORDER_EXTRA_FURSUITS))
                    .requireAttention(record.get(ORDERS.ORDER_REQUIRES_ATTENTION))
                    .checkinText(record.get(ORDERS.ORDER_CHECKIN_TEXT))
                    .internalComment(record.get(ORDERS.ORDER_INTERNAL_COMMENT))
                    .orderSerialInEvent(record.get(ORDERS.ORDER_SERIAL_IN_EVENT))
                    .orderOwnerUserId(userId)
                    .eventId(record.get(ORDERS.EVENT_ID))
                    .userFinder(userFinder)
                    .eventFinder(eventFinder);

            if (loadAnswers) {
                // Keep in mind that only if the order is from the current event we can load the answers
                final var answerList = objectMapper.readValue(
                        record.get(ORDERS.ORDER_ANSWERS_JSON).data(),
                        pretixAnswerRef
                );
                v.answers(answerList, pretixInformation);
            } else {
                v.emptyAnswers();
            }

            return v.build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
