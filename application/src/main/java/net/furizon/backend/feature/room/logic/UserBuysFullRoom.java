package net.furizon.backend.feature.room.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.action.pushAnswer.PushPretixAnswerAction;
import net.furizon.backend.feature.pretix.objects.order.action.pushPosition.PushPretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.action.updatePosition.UpdatePretixPositionAction;
import net.furizon.backend.feature.pretix.objects.payment.PretixPayment;
import net.furizon.backend.feature.pretix.objects.payment.action.manualRefundPayment.ManualRefundPaymentAction;
import net.furizon.backend.feature.pretix.objects.payment.action.yeetPayment.IssuePaymentAction;
import net.furizon.backend.feature.pretix.objects.refund.PretixRefund;
import net.furizon.backend.feature.pretix.objects.refund.action.yeetRefund.IssueRefundAction;
import net.furizon.backend.feature.pretix.objects.order.dto.PushPretixPositionRequest;
import net.furizon.backend.feature.pretix.objects.order.dto.UpdatePretixPositionRequest;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixPositionFinder;
import net.furizon.backend.feature.pretix.objects.order.usecase.UpdateOrderInDb;
import net.furizon.backend.feature.pretix.objects.payment.finder.PretixPaymentFinder;
import net.furizon.backend.feature.pretix.objects.refund.finder.PretixRefundFinder;
import net.furizon.backend.feature.room.dto.RoomErrorCodes;
import net.furizon.backend.feature.room.dto.response.RoomGuestResponse;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.usecase.RoomChecks;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "room", name = "logic", havingValue = "roomLogic-user-buys-full-room")
public class UserBuysFullRoom implements RoomLogic {
    @NotNull private final RoomChecks checks;
    @NotNull private final UpdateOrderInDb updateOrderInDb;
    @NotNull private final DefaultRoomLogic defaultRoomLogic;
    @NotNull private final PretixOrderFinder pretixOrderFinder;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final RoomFinder roomFinder;


    //Transfer full order related stuff
    @NotNull private final PretixRefundFinder pretixRefundFinder;
    @NotNull private final PretixPaymentFinder pretixPaymentFinder;
    @NotNull private final PushPretixAnswerAction pushPretixAnswerAction;
    @NotNull private final ManualRefundPaymentAction refundPaymentAction;

    //Exchange room related stuff
    @NotNull private final PretixPositionFinder pretixPositionFinder;
    @NotNull private final PushPretixPositionAction pushPretixPositionAction;
    @NotNull private final UpdatePretixPositionAction updatePretixPositionAction;
    @NotNull private final IssuePaymentAction issuePaymentAction;
    @NotNull private final IssueRefundAction issueRefundAction;

    @Override
    public boolean canCreateRoom(long userId, @NotNull Event event) {
        var r = orderFinder.userHasBoughtAroom(userId, event);
        return r.isPresent() && r.get() && defaultRoomLogic.canCreateRoom(userId, event);
    }

    @Override
    public long createRoom(String name, long userId, @NotNull Event event) {
        checks.assertUserHasBoughtAroom(userId, event);
        return defaultRoomLogic.createRoom(name, userId, event);
    }

    @Override
    public boolean deleteRoom(long roomId) {
        return defaultRoomLogic.deleteRoom(roomId);
    }

    @Override
    public boolean changeRoomName(String name, long roomId) {
        return defaultRoomLogic.changeRoomName(name, roomId);
    }

    @Override
    public long invitePersonToRoom(
            long invitedUserId, long roomId, @NotNull Event event, boolean force, boolean forceExit) {
        checks.assertRoomNotFull(roomId, true);
        checks.assertUserHasNotBoughtAroom(invitedUserId, event);
        return defaultRoomLogic.invitePersonToRoom(invitedUserId, roomId, event, force, forceExit);
    }

    @Override
    public boolean inviteAccept(long guestId, long invitedUserId, long roomId, @NotNull Event event) {
        checks.assertRoomNotFull(roomId, true);
        checks.assertUserHasNotBoughtAroom(invitedUserId, event);
        return defaultRoomLogic.inviteAccept(guestId, invitedUserId, roomId, event);
    }

    @Override
    public boolean inviteRefuse(long guestId) {
        return defaultRoomLogic.inviteRefuse(guestId);
    }

    @Override
    public boolean inviteCancel(long guestId) {
        return defaultRoomLogic.inviteCancel(guestId);
    }

    @Override
    public boolean kickFromRoom(long guestId) {
        return defaultRoomLogic.kickFromRoom(guestId);
    }

    @Override
    public boolean leaveRoom(long guestId) {
        return defaultRoomLogic.leaveRoom(guestId);
    }

    @Override
    public boolean isConfirmationSupported() {
        return false;
    }

    @Override
    public boolean canConfirmRoom(long roomId, @NotNull Event event) {
        return false;
    }

    @Override
    public boolean canUnconfirmRoom(long roomId) {
        return false;
    }

    @Override
    public boolean confirmRoom(long roomId) {
        return defaultRoomLogic.confirmRoom(roomId);
    }

    @Override
    public boolean isUnconfirmationSupported() {
        return false;
    }

    @Override
    public boolean unconfirmRoom(long roomId) {
        return false;
    }

    @Override
    public boolean exchangeRoom(long targetUsrId, long sourceUsrId, long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.info("[ROOM_EXCHANGE] UserBuysFullRoom: Exchange between users: {} -> {} on event {}",
                sourceUsrId, targetUsrId, event);
        //get orders
        Order sourceOrder = orderFinder.findOrderByUserIdEvent(sourceUsrId, event, pretixInformation);
        Order targetOrder = orderFinder.findOrderByUserIdEvent(targetUsrId, event, pretixInformation);
        if (sourceOrder == null) {
            log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: Source has no order",
                    sourceUsrId, targetUsrId, event);
            return false;
        }
        if (targetOrder == null) {
            log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: Target has no order",
                    sourceUsrId, targetUsrId, event);
            return false;
        }
        String sourceOrderCode = sourceOrder.getCode();
        String targetOrderCode = targetOrder.getCode();

        //Get how much source user has paid for his room and how much does it costs now
        Long sourceRoomPositionId = sourceOrder.getRoomPositionId();
        if (sourceRoomPositionId == null) {
            log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No sourceRoomPositionId",
                    sourceUsrId, targetUsrId, event);
            return false;
        }
        var sp = pretixPositionFinder.fetchPositionById(event, sourceRoomPositionId);
        if (!sp.isPresent()) {
            log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No source room position",
                    sourceUsrId, targetUsrId, event);
            return false;
        }
        long sourcePaid = PretixGenericUtils.fromStrPriceToLong(sp.get().getPrice());

        Long sourceRoomItemId = sourceOrder.getPretixRoomItemId();
        if (sourceRoomItemId == null) {
            log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No sourceRoomItemId",
                    sourceUsrId, targetUsrId, event);
            return false;
        }
        Long sourcePrice = pretixInformation.getRoomPriceByItemId(sourceRoomItemId, true);
        if (sourcePrice == null) {
            log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No source room price",
                    sourceUsrId, targetUsrId, event);
            return false;
        }

        boolean targetHasRoomItem = false;
        long targetPaid = 0L;
        Long targetPrice = 0L;

        Long targetRoomItemId = targetOrder.getPretixRoomItemId();
        Long targetRoomPositionId = targetOrder.getRoomPositionId();
        if (targetRoomItemId != null && targetRoomPositionId != null) {
            //We should not create a new position on targetUser but change the existing one
            targetHasRoomItem = true;

            //Get how much the target user has paid for his room and how much does it costs now
            //This works also if the target user has a NO_ROOM
            var tp = pretixPositionFinder.fetchPositionById(event, targetRoomPositionId);
            if (!tp.isPresent()) {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No target room position",
                        sourceUsrId, targetUsrId, event);
                return false;
            }
            targetPaid = PretixGenericUtils.fromStrPriceToLong(tp.get().getPrice());

            targetPrice = pretixInformation.getRoomPriceByItemId(targetRoomItemId, true);
            if (targetPrice == null) {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No target room price",
                        sourceUsrId, targetUsrId, event);
                return false;
            }

        } else {
            //If the target user doesn't have a room in his order, default it with NO_ROOM item
            targetRoomItemId = (Long) pretixInformation.getIdsForItemType(CacheItemTypes.NO_ROOM_ITEM).toArray()[0];
        }

        //Now that we have everything, update pretix
        boolean res = true;

        //Update target position
        if (targetHasRoomItem) {
            res = res && updatePretixPositionAction.invoke(event, targetRoomPositionId, new UpdatePretixPositionRequest(
                targetOrderCode,
                sourceRoomItemId
            ));
            defaultRoomLogic.logExchangeError(res, 100, targetUsrId, sourceUsrId, event);
        } else {
            res = res && pushPretixPositionAction.invoke(event, new PushPretixPositionRequest(
                targetOrderCode,
                targetOrder.getTicketPositionId(),
                sourceRoomItemId
            ));
            defaultRoomLogic.logExchangeError(res, 101, targetUsrId, sourceUsrId, event);
        }

        //Update source position
        res = res && updatePretixPositionAction.invoke(event, sourceRoomPositionId, new UpdatePretixPositionRequest(
                sourceOrderCode,
                sourceRoomItemId
        ));
        defaultRoomLogic.logExchangeError(res, 102, targetUsrId, sourceUsrId, event);

        //Calculate payment/refunds total. If balance > 0 we need to issue a refund, otherwise a payment
        long sourceBalance = sourcePaid - targetPrice;
        long targetBalance = targetPaid - sourcePrice;

        String comment = " was created for a room exchange between orders " + sourceOrderCode + " -> " + targetOrderCode
                + " happened on timestamp " + System.currentTimeMillis();
        BiFunction<Long, String, Boolean> issuePaymentOrRefund = (balance, orderCode) -> {
            String amount = PretixGenericUtils.fromPriceToString(balance, '.');
            if (balance > 0L) {
                return issueRefundAction.invoke(event, orderCode, "Refund" + comment, amount);
            } else if (balance < 0L) {
                return issuePaymentAction.invoke(event, orderCode, "Payment" + comment, amount);
            } //Else balance == 0: do nothing, we're done
            return false;
        };
        //Issue payments or refunds
        res = res && issuePaymentOrRefund.apply(targetBalance, targetOrderCode);
        defaultRoomLogic.logExchangeError(res, 103, targetUsrId, sourceUsrId, event);
        res = res && issuePaymentOrRefund.apply(sourceBalance, sourceOrderCode);
        defaultRoomLogic.logExchangeError(res, 104, targetUsrId, sourceUsrId, event);

        //Exchange rooms in db
        res = res && defaultRoomLogic.exchangeRoom(targetUsrId, sourceUsrId, roomId, event, pretixInformation);
        defaultRoomLogic.logExchangeError(res, 105, targetUsrId, sourceUsrId, event);

        if (res) {
            var pair = event.getOrganizerAndEventPair();
            String eventName = pair.getEvent();
            String organizerName = pair.getOrganizer();
            Function<String, Boolean> fetchAndUpdateOrder = (orderCode) -> {
                var order = pretixOrderFinder.fetchOrderByCode(organizerName, eventName, orderCode);
                if (!order.isPresent()) {
                    log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: Unable to refetch order {} from pretix",
                            sourceUsrId, targetUsrId, event, orderCode);
                    return false;
                }
                return updateOrderInDb.execute(order.get(), event, pretixInformation).isPresent();
            };

            //Fully reload orders from pretix to the DB
            res = res && fetchAndUpdateOrder.apply(targetOrderCode);
            defaultRoomLogic.logExchangeError(res, 106, targetUsrId, sourceUsrId, event);
            res = res && fetchAndUpdateOrder.apply(sourceOrderCode);
            defaultRoomLogic.logExchangeError(res, 107, targetUsrId, sourceUsrId, event);
        }

        return res;
    }

    @Override
    public boolean exchangeFullOrder(long targetUsrId, long sourceUsrId, long roomId,
                                     @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.info("[ORDER_TRANSFER] UserBuysFullRoom: Trasferring order {} -> {} on event {}",
                sourceUsrId, targetUsrId, event);

        //Fetch source order
        Order sourceOrder = orderFinder.findOrderByUserIdEvent(sourceUsrId, event, pretixInformation);
        if (sourceOrder == null) {
            log.error("[ORDER_TRANSFER] {} -> {} on event {}: Unable to find order for user {}",
                    sourceUsrId, targetUsrId, event, sourceUsrId);
            return false;
        }
        String orderCode = sourceOrder.getCode();

        //We're now gonna invalidate all previous payments and then create one "aggregate" manual
        // payment which cannot be refunded. This is for preventing pretix admins from issueing a refund
        // after the order has been transferred. If this happens, the original order buyer would get the money
        // instead of the new owner

        //Fetch payments and refunds of source order
        List<PretixPayment> payments = pretixPaymentFinder.getPaymentsForOrder(event, sourceOrder.getCode());
        List<PretixRefund> refunds = pretixRefundFinder.getRefundsForOrder(event, sourceOrder.getCode());
        Map<String, Long> balanceForProvider = new HashMap<>();

        //For each payment provider, calc how much the user has paid with them
        for (PretixPayment payment : payments) {
            PretixPayment.PaymentState state = payment.getState();
            if (state == PretixPayment.PaymentState.CONFIRMED) {
                String provider = payment.getProvider();
                Long balance = balanceForProvider.get(provider);
                if (balance == null) {
                    balance = 0L;
                }

                balance += PretixGenericUtils.fromStrPriceToLong(payment.getAmount());
                balanceForProvider.put(provider, balance);

            } else if (state == PretixPayment.PaymentState.PENDING || state == PretixPayment.PaymentState.CREATED) {
                log.error("[ORDER_TRANSFER] {} -> {} on event {}: "
                        + "No payments for order {} can be in status PENDING or CREATED. Failed payment: {} {}",
                        sourceUsrId, targetUsrId, event, orderCode, payment.getId(), state);
                throw new ApiException("Payment found in illegal state", RoomErrorCodes.ORDER_PAYMENTS_STILL_PENDING);
            }
        }

        //For each payment provider, subtract how much it was already refunded from it
        for (PretixRefund refund : refunds) {
            PretixRefund.RefundState state = refund.getState();
            if (state == PretixRefund.RefundState.DONE) {
                String provider = refund.getProvider();
                long balance = balanceForProvider.get(provider);
                balance -= PretixGenericUtils.fromStrPriceToLong(refund.getAmount());
                balanceForProvider.put(provider, balance);

            } else if (
                    state == PretixRefund.RefundState.CREATED
                    || state == PretixRefund.RefundState.TRANSIT
                    || state == PretixRefund.RefundState.EXTERNAL
            ) {
                log.error("[ORDER_TRANSFER] {} -> {} on event {}: "
                        + "No refunds for order {} can be in status CREATED, TRANSIT or EXTERNAL. Failed refund: {} {}",
                        sourceUsrId, targetUsrId, event, orderCode, refund.getId(), state);
                throw new ApiException("Refund found in illegal state", RoomErrorCodes.ORDER_REFUNDS_STILL_PENDING);
            }
        }

        //Get total to refund
        long total = balanceForProvider.values().stream().mapToLong(Long::longValue).sum();

        //For each payment provider, issue a refund
        for (PretixPayment payment : payments) {
            if (payment.getState() == PretixPayment.PaymentState.CONFIRMED) {
                String provider = payment.getProvider();
                long balance = balanceForProvider.get(provider);
                long paymentAmount = PretixGenericUtils.fromStrPriceToLong(payment.getAmount());

                //If we're already done with refunding this provider, don't do it
                if (balance > 0L) {
                    //Take the minimum between the payment and the left balance
                    long toRefund = Math.min(balance, paymentAmount);
                    boolean success = refundPaymentAction.invoke(event, orderCode, payment.getId(), toRefund);
                    if (success) {
                        balance -= toRefund;
                    } else {
                        log.warn("[ORDER_TRANSFER] {} -> {} on event {}:"
                            + "Unable to refund payment {} of order {} of "
                            + "{} amount of money. Balance = {}; paymentAmount = {}",
                            sourceUsrId, targetUsrId, event, payment.getId(),
                            orderCode, toRefund, balance, paymentAmount);
                    }
                }
                balanceForProvider.put(provider, balance);
            }
        }

        //If we missed something to refund, exit with error
        long leftToRefund = balanceForProvider.values().stream().mapToLong(Long::longValue).sum();
        if (leftToRefund > 0L) {
            log.error("[ORDER_TRANSFER] {} -> {} on event {}: Unable to finish refunding order {}. Left = {}",
                    sourceUsrId, targetUsrId, event, orderCode, leftToRefund);
            return false;
        }

        //Create a new manual payment which cannot be refunded
        boolean res = issuePaymentAction.invoke(
                event,
                orderCode,
                "Payment was created for a order exchange between users " + sourceUsrId + " -> " + targetUsrId
                        + " happened on timestamp " + System.currentTimeMillis()
                        + ". DO NOT REFUND ANY PAYMENT FROM THIS ORDER!",
                PretixGenericUtils.fromPriceToString(total, '.')
        );
        defaultRoomLogic.logExchangeError(res, 200, targetUsrId, sourceUsrId, event);

        //Update userId on order and push it to pretix
        sourceOrder.setOrderOwnerUserId(targetUsrId);
        res = res && pushPretixAnswerAction.invoke(sourceOrder, pretixInformation);
        defaultRoomLogic.logExchangeError(res, 201, targetUsrId, sourceUsrId, event);

        //Changes in DB
        res = res && defaultRoomLogic.exchangeFullOrder(targetUsrId, sourceUsrId, roomId, event, pretixInformation);
        defaultRoomLogic.logExchangeError(res, 202, targetUsrId, sourceUsrId, event);

        //Refetch pretix order and store it in db
        if (res) {
            var pair = event.getOrganizerAndEventPair();
            var order = pretixOrderFinder.fetchOrderByCode(pair.getOrganizer(), pair.getEvent(), orderCode);
            if (!order.isPresent()) {
                log.error("[ORDER_TRANSFER] {} -> {} on event {}: Unable to refetch order {} from pretix",
                        sourceUsrId, targetUsrId, event, orderCode);
                return false;
            }
            res = updateOrderInDb.execute(order.get(), event, pretixInformation).isPresent();
            defaultRoomLogic.logExchangeError(res, 203, targetUsrId, sourceUsrId, event);
        }

        return res;
    }

    @Override
    public boolean refundRoom(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.warn("Refund room not supported with UserBuysFullRoom");
        return false;
    }

    @Override
    public boolean refundFullOrder(long userId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.warn("Refund full order not supported with UserBuysFullRoom");
        return false;
    }

    @Override
    public boolean isRoomBuyOrUpgradeSupported(@NotNull Event even) {
        return true;
    }

    @Override
    public boolean buyOrUpgradeRoom(long newRoomItemId, long userId, @Nullable Long roomId, @NotNull Order order, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        Long positionId = order.getRoomPositionId();

        pretixInformation.getAvailabilityFromItemId(newRoomItemId);
        
        return false;
    }

    private void sanityCheckLogAndStoreErrors(@Nullable List<String> logbook, String message, Object... args) {
        log.error(message, args);
        if (logbook != null) {
            logbook.add(message);
        }
    }

    @Override
    public void doSanityChecks(long roomId, @NotNull PretixInformation pretixInformation,
                               @Nullable List<String> detectedErrors) {
        log.info("[ROOM SANITY CHECKS] Running room sanity check on room {}", roomId);
        Event event = pretixInformation.getCurrentEvent();
        long eventId = event.getId();
        //The following checks are done:
        //- User in multiple rooms
        //- Members exceed room maximum
        //- Owner not in room
        //- Owner has multiple rooms
        //- Owner order status == canceled
        //- Owner doesn't own a room
        //- Owner has a daily ticket
        //- User order status == canceled
        //- User order is daily

        //RoomInfo info = roomFinder.getRoomInfoForUser(userId, input.event, input.pretixInformation);
        List<RoomGuestResponse> guests = roomFinder.getRoomGuestResponseFromRoomId(roomId, event);
        List<RoomGuestResponse> confirmedGuests = guests.stream().filter(k -> k.getRoomGuest().isConfirmed()).toList();


        var ownerIdOpt = roomFinder.getOwnerUserIdFromRoomId(roomId);
        if (!ownerIdOpt.isPresent()) {
            //delete room, no owner found
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "No owner found in room {}. Deleting the room", roomId);
            this.deleteRoom(roomId);
            return;
        }
        long ownerId = ownerIdOpt.get();
        Order order = orderFinder.findOrderByUserIdEvent(ownerId, event, pretixInformation);
        if (order == null) {
            //delete room, owner has no order
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "Owner {} of room {} has no order. Deleting the room", ownerId, roomId);
            this.deleteRoom(roomId);
            return;
        }
        if (!order.hasRoom()) {
            //delete room, owner has no room
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "Owner {} of room {} hasn't bought a room. Deleting the room", ownerId, roomId);
            this.deleteRoom(roomId);
            return;
        }
        if (order.isDaily()) {
            //delete room, owner has daily ticket
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "Owner {} of room {} has a daily ticket. Deleting the room", ownerId, roomId);
            this.deleteRoom(roomId);
            return;
        }

        int guestNo = confirmedGuests.size();
        int capacity = (int) order.getRoomCapacity();
        if (guestNo > capacity) {
            //delete room, too many members!
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "Room {} has too many members ({} > {}). Deleting the room", roomId, guestNo, capacity);
            this.deleteRoom(roomId);
            return;
        }

        boolean ownerFound = false;
        for (RoomGuestResponse guest : confirmedGuests) {
            long usrId = guest.getUser().getUserId();
            long guestId = guest.getRoomGuest().getGuestId();

            //This (together with owner in room check) will also test if a owner has multiple rooms
            int roomNo = roomFinder.countRoomsWithUser(usrId, eventId);
            if (roomNo > 1) {
                if (usrId == ownerId) {
                    //delete room, owner is in too many rooms
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "Owner {} of room {} is in too many rooms ({})!. Deleting the room", usrId, roomId, roomNo);
                    this.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, he's in too many rooms
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "User {}g{} of room {} is in too many rooms ({})!. Kicking the user",
                        usrId, guestId, roomId, roomNo);
                    this.kickFromRoom(guestId);
                    continue;
                }
            }

            OrderStatus status = guest.getOrderStatus();
            if (status == null || status == OrderStatus.CANCELED) {
                if (usrId == ownerId) {
                    //delete room, owner's order is canceled
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "Owner {} of room {} has a canceled order!. Deleting the room", usrId, roomId);
                    this.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, his order is canceled
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "User {}g{} of room {} has a canceled order!. Kicking the user", usrId, guestId, roomId);
                    this.kickFromRoom(guestId);
                    continue;
                }
            }

            var daily = orderFinder.isOrderDaily(usrId, event);
            if (!daily.isPresent()) {
                if (usrId == ownerId) {
                    //delete room, owner has no order
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "Owner {} of room {} has no order. Deleting the room", usrId, roomId);
                    this.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, user has no order
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "User {}g{} of room {} has no order. Kicking the user", usrId, guestId, roomId);
                    this.kickFromRoom(guestId);
                    continue;
                }
            }
            if (daily.get()) {
                if (usrId == ownerId) {
                    //delete room, owner has a daily ticket
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "Owner {} of room {} has a daily ticket. Deleting the room", usrId, roomId);
                    this.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, he has a daily ticket
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "User {}g{} of room {} has a daily ticket. Kicking the user", usrId, guestId, roomId);
                    this.kickFromRoom(guestId);
                    continue;
                }
            }

            if (usrId == ownerId) {
                ownerFound = true;
            }
        }

        if (!ownerFound) {
            //delete room, owner is not in room!
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                + "Owner {} of room {} was not found in the room's guest. Deleting the room", ownerId, roomId);
            this.deleteRoom(roomId);
            return;
        }
    }
}
