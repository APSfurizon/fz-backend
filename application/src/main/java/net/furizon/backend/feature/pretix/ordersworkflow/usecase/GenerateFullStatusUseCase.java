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
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.feature.room.logic.RoomLogic;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.RoomConfig;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateFullStatusUseCase implements UseCase<GenerateFullStatusUseCase.Input, FullInfoResponse> {
    @NotNull private final RoomLogic roomLogic;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final MembershipCardFinder membershipCardFinder;
    @NotNull private final PretixConfig pretixConfig;
    @NotNull private final SanityCheck sanityCheck;
    @NotNull private final RoomConfig roomConfig;

    @Override
    public @NotNull FullInfoResponse executor(@NotNull Input input) {
        Event event = input.pretixInformation.getCurrentEvent();
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
                        new RoomData(
                            roomCapacity,
                            input.pretixInformation.getRoomNamesFromRoomPretixItemId(
                                    Objects.requireNonNull(order.getPretixRoomItemId())
                            )
                        )
                    );
            }

            orderDataResponse = orderDataBuilder.build();
        }

        OffsetDateTime startBooking = pretixConfig.getEvent().getPublicBookingStartTime();
        boolean displayCountdown = true && OffsetDateTime.now().isBefore(startBooking); //TODO [ADMIN_CHECK] //TODO [STAFFER_CHECK]

        OffsetDateTime endRoomEditingTime = roomConfig.getRoomChangesEndTime();
        boolean roomEditingTimeAllowed = endRoomEditingTime == null || endRoomEditingTime.isAfter(OffsetDateTime.now());
        boolean canBuyOrUpgradeRoom = roomEditingTimeAllowed && roomLogic.isRoomBuyOrUpgradeSupported(event);

        return FullInfoResponse.builder()
            .bookingStartTime(startBooking)
            .shouldDisplayCountdown(displayCountdown)
            .editBookEndTime(pretixConfig.getEvent().getEditBookingEndTime())
            .hasActiveMembershipForEvent(membershipNo > 0)
            .canBuyOrUpgradeRoom(canBuyOrUpgradeRoom)
            .eventNames(event.getEventNames())
            .order(orderDataResponse)
            .errors(errors)
            .build();
    }

    public record Input(
            @NotNull PretixInformation pretixInformation,
            @NotNull FurizonUser user
    ){}
}
