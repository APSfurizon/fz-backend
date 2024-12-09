package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.FullInfoResponse;
import net.furizon.backend.feature.pretix.ordersworkflow.dto.OrderDataResponse;
import net.furizon.backend.feature.room.dto.response.RoomDataResponse;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateFullStatusUseCase implements UseCase<GenerateFullStatusUseCase.Input, FullInfoResponse> {
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final MembershipCardFinder membershipCardFinder;
    @NotNull private final SanityCheck sanityCheck;

    @Value("${pretix.event.public-booking-start-time}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Nullable private OffsetDateTime publicBookingStartTime;

    @Value("${pretix.event.edit-booking-end-time}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Nullable private OffsetDateTime editBookingEndTime;

    @Override
    public @NotNull FullInfoResponse executor(@NotNull Input input) {
        var e = input.pretixInformation.getCurrentEvent();
        if (!e.isPresent()) {
            log.error("Event is null!");
            throw new RuntimeException("Event is null");
        }
        Event event = e.get();
        FurizonUser user = input.user;

        int ordersNo = orderFinder.countOrdersOfUserOnEvent(user.getUserId(), event);
        int membershipNo = membershipCardFinder.countCardsPerUserPerEvent(user.getUserId(), event);
        List<OrderWorkflowErrorCode> errors = new LinkedList<>();
        sanityCheck.execute(ordersNo, membershipNo, errors);

        OrderDataResponse orderDataResponse = null;
        Order order = orderFinder.findOrderByUserIdEvent(user.getUserId(), event, input.pretixInformation);
        if (order != null) {
            boolean isDaily = order.isDaily();
            var orderDataBuilder = OrderDataResponse.builder()
                .code(order.getCode())
                .orderStatus(order.getOrderStatus())
                .sponsorship(order.getSponsorship())
                .extraDays(order.getExtraDays())
                .isDailyTicket(isDaily);

            OffsetDateTime from = event.getDateFrom();
            if (isDaily && from != null) {
                orderDataBuilder = orderDataBuilder.dailyDays(
                        order.getDailyDays().stream().map(
                                d -> from.plusDays(d).toLocalDate()
                        ).collect(Collectors.toSet())
                );
            }
            if (order.hasRoom()) {
                short roomCapacity = order.getRoomCapacity();
                orderDataBuilder = orderDataBuilder.room(
                        new RoomDataResponse(
                            roomCapacity,
                            input.pretixInformation.getRoomNamesFromInfo(order.getHotelInternalName(), roomCapacity)
                        )
                    );
            }

            orderDataResponse = orderDataBuilder.build();
        }

        return FullInfoResponse.builder()
            .shouldDisplayCountdown(true) //TODO [ADMIN_CHECK] //TODO [STAFFER_CHECK]
            .bookingStartTime(publicBookingStartTime)
            .editBookEndTime(editBookingEndTime)
            .eventNames(event.getEventNames())
            .hasActiveMembershipForEvent(membershipNo > 0)
            .order(orderDataResponse)
            .errors(errors)
            .build();
    }

    public record Input(
            @NotNull PretixInformation pretixInformation,
            @NotNull FurizonUser user
    ){}
}
