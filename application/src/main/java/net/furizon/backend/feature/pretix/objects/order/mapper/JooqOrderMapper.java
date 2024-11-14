package net.furizon.backend.feature.pretix.objects.order.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.mapper.JooqEventMapper;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixAnswer;
import net.furizon.backend.feature.user.mapper.JooqUserMapper;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.USERS;

@Component
@RequiredArgsConstructor
public class JooqOrderMapper {
    private final TypeReference<List<PretixAnswer>> pretixAnswerRef = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    private final PretixInformation pretixInformation;

    private final JooqEventMapper jooqEventMapper;

    @NotNull
    public Order map(Record record) {
        try {
            final var answerList = objectMapper.readValue(
                record.get(ORDERS.ORDER_ANSWERS_JSON).data(),
                pretixAnswerRef
            );
            return Order.builder()
                .code(record.get(ORDERS.ORDER_CODE))
                .orderStatus(OrderStatus.values()[record.get(ORDERS.ORDER_STATUS)])
                .sponsorship(Sponsorship.values()[record.get(ORDERS.ORDER_SPONSORSHIP_TYPE)])
                .extraDays(ExtraDays.values()[record.get(ORDERS.ORDER_EXTRA_DAYS_TYPE)])
                .dailyDays(record.get(ORDERS.ORDER_DAILY_DAYS))
                .roomCapacity(record.get(ORDERS.ORDER_ROOM_CAPACITY)) //TODO change after db regeneration
                .hotelLocation(record.get(ORDERS.ORDER_HOTEL_LOCATION))
                .pretixOrderSecret(record.get(ORDERS.ORDER_SECRET))
                .hasMembership(record.get(ORDERS.HAS_MEMBERSHIP))
                .answersMainPositionId(record.get(ORDERS.ORDER_ANSWERS_MAIN_POSITION_ID))
                .orderOwner(record.get(USERS.USER_ID) != null ? JooqUserMapper.map(record) : null)
                .orderEvent(jooqEventMapper.map(record))
                .answers(answerList, pretixInformation)
                .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
