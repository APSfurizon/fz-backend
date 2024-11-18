package net.furizon.backend.feature.pretix.objects.order.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.finder.EventFinder;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixAnswer;
import net.furizon.backend.feature.user.finder.UserFinder;
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
    public Order map(Record record, PretixInformation pretixInformation) {
        try {
            final var answerList = objectMapper.readValue(
                record.get(ORDERS.ORDER_ANSWERS_JSON).data(),
                pretixAnswerRef
            );
            Long userId = record.get(ORDERS.USER_ID);
            return Order.builder()
                .code(record.get(ORDERS.ORDER_CODE))
                .orderStatus(OrderStatus.values()[record.get(ORDERS.ORDER_STATUS)])
                .sponsorship(Sponsorship.values()[record.get(ORDERS.ORDER_SPONSORSHIP_TYPE)])
                .extraDays(ExtraDays.values()[record.get(ORDERS.ORDER_EXTRA_DAYS_TYPE)])
                .dailyDays(record.get(ORDERS.ORDER_DAILY_DAYS))
                .roomCapacity(record.get(ORDERS.ORDER_ROOM_CAPACITY))
                .hotelLocation(record.get(ORDERS.ORDER_HOTEL_LOCATION))
                .pretixOrderSecret(record.get(ORDERS.ORDER_SECRET))
                .hasMembership(record.get(ORDERS.HAS_MEMBERSHIP))
                .answersMainPositionId(record.get(ORDERS.ORDER_ANSWERS_MAIN_POSITION_ID))
                .answers(answerList, pretixInformation)
                .orderOwnerUserId(userId == null ? -1L : userId)
                .eventId(record.get(ORDERS.EVENT_ID))
                .userFinder(userFinder)
                .eventFinder(eventFinder)
                .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
