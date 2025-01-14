package net.furizon.backend.feature.room.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.action.deletePosition.DeletePretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.action.pushAnswer.PushPretixAnswerAction;
import net.furizon.backend.feature.pretix.objects.order.action.pushPosition.PushPretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.action.updatePosition.UpdatePretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.backend.feature.pretix.objects.payment.PretixPayment;
import net.furizon.backend.feature.pretix.objects.payment.action.manualRefundPayment.ManualRefundPaymentAction;
import net.furizon.backend.feature.pretix.objects.payment.action.yeetPayment.IssuePaymentAction;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
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
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.rooms.MailRoomService;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.furizon.backend.infrastructure.email.EmailVars.OTHER_FURSONA_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.ROOM_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.SANITY_CHECK_REASON;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_NO_OWNER;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_OWNER_HAS_DAILY_TICKET;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_OWNER_HAS_NOT_BOUGHT_ROOM;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_OWNER_HAS_NO_ORDER;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_OWNER_IN_TOO_MANY_ROOMS;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_OWNER_NOT_IN_ROOM;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_OWNER_ORDER_INVALID_STATUS;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_TOO_MANY_MEMBERS;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_USER_HAS_DAILY_TICKET;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_USER_HAS_NO_ORDER;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_USER_IN_TOO_MANY_ROOMS;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.SC_USER_ORDER_INVALID_ORDER_STATUS;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_SANITY_CHECK_DELETED;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_SANITY_CHECK_KICK_OWNER;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_SANITY_CHECK_KICK_USER;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "room", name = "logic", havingValue = "roomLogic-user-buys-full-room")
public class UserBuysFullRoom implements RoomLogic {
    @NotNull private final RoomChecks checks;
    @NotNull private final MailRoomService mailService;
    @NotNull private final UpdateOrderInDb updateOrderInDb;
    @NotNull private final DefaultRoomLogic defaultRoomLogic;
    @NotNull private final PretixOrderFinder pretixOrderFinder;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final UserFinder userFinder;

    //Buy or upgrade related stuff
    @NotNull private final DeletePretixPositionAction deletePretixPositionAction;

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

    private @NotNull Map<String, Long> getBalanceForProvider(@NotNull String orderCode, @NotNull Event event) {
        List<PretixPayment> payments = pretixPaymentFinder.getPaymentsForOrder(event, orderCode);
        List<PretixRefund> refunds = pretixRefundFinder.getRefundsForOrder(event, orderCode);
        return getBalanceForProvider(payments, refunds, orderCode, event);
    }
    private @NotNull Map<String, Long> getBalanceForProvider(@NotNull List<PretixPayment> payments,
                                                             @NotNull List<PretixRefund> refunds,
                                                             @NotNull String orderCode, @NotNull Event event) {
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
                log.error("No payments for order {} on event {} "
                        + "can be in status PENDING or CREATED. Failed payment: {} {}",
                        orderCode, event, payment.getId(), state);
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
                log.error("No refunds for order {} on event {} "
                        + "can be in status CREATED, TRANSIT or EXTERNAL. Failed refund: {} {}",
                        orderCode, event, refund.getId(), state);
                throw new ApiException("Refund found in illegal state", RoomErrorCodes.ORDER_REFUNDS_STILL_PENDING);
            }
        }

        return balanceForProvider;
    }

    private record ExchangeResult(
            long sourceBalance,
            long targetBalance,
            @Nullable Long sourcePositionId,
            @Nullable Long targetPositionId,
            @Nullable Long sourcePosid,
            @Nullable Long targetPosid
    ) {}

    private @Nullable ExchangeResult exchangeSingleItem(
            @Nullable Long sourceItemId,
            @Nullable Long sourcePositionId,
            @Nullable Long targetItemId,
            @Nullable Long targetPositionId,
            @Nullable Long sourceAddonToPositionId,
            @Nullable Long targetAddonToPositionId,
            long sourceUsrId,
            long targetUsrId,
            @NotNull String sourceOrderCode,
            @NotNull String targetOrderCode,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation,
            boolean createTempPositionFirst
    ) {
        log.info("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: Exchanging i{} p{} a{} -> i{} p{} a{}",
                sourceUsrId, targetUsrId, event, sourceItemId, sourcePositionId, sourceAddonToPositionId,
                targetItemId, targetPositionId, targetAddonToPositionId);
        //Fetch source paid and price
        long sourcePaid = 0L;
        Long sourcePrice = 0L;
        boolean sourceHasPosition = sourcePositionId != null && sourceItemId != null;
        if (sourceHasPosition) {
            //Fetch how much source has paid the product
            var sp = pretixPositionFinder.fetchPositionById(event, sourcePositionId);
            if (!sp.isPresent()) {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No source position for id {}",
                        sourceUsrId, targetUsrId, event, sourceItemId);
                return null;
            }
            sourcePaid = PretixGenericUtils.fromStrPriceToLong(sp.get().getPrice());
            //Fetch how much source item costs (It can be different from how much user has paid!)
            sourcePrice = pretixInformation.getItemPrice(sourceItemId, true);
            if (sourcePrice == null) {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No source room price for item {}",
                        sourceUsrId, targetUsrId, event, sourceItemId);
                return null;
            }
        }

        //Fetch target paid and price
        long targetPaid = 0L;
        Long targetPrice = 0L;
        boolean targetHasPosition = targetPositionId != null && targetItemId != null;
        if (targetHasPosition) {
            //Get how much the target user has paid for his room and how much does it costs now
            //This works also if the target user has a NO_ROOM
            var tp = pretixPositionFinder.fetchPositionById(event, targetPositionId);
            if (!tp.isPresent()) {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No target position for id {}",
                        sourceUsrId, targetUsrId, event, sourceItemId);
                return null;
            }
            targetPaid = PretixGenericUtils.fromStrPriceToLong(tp.get().getPrice());

            targetPrice = pretixInformation.getItemPrice(targetItemId, true);
            if (targetPrice == null) {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No target price for item {}",
                        sourceUsrId, targetUsrId, event, targetItemId);
                return null;
            }
        }

        //We don't check for hasPosition but for the itemId != null, because NO_ROOM has an itemId but no positionId
        //Update target
        Long toDeletePositionId = null; //used for keeping quota reserved while performing the exchange
        Long sourcePosid = null;
        boolean res = true;
        if (sourceItemId != null) {
            if (targetHasPosition) {
                //Creates a temporary position with target's item to keep quota reserved while performing the exchange
                PretixPosition pp = pushPretixPositionAction.invoke(event, new PushPretixPositionRequest(
                        targetOrderCode,
                        null,
                        targetItemId
                ), createTempPositionFirst, pretixInformation, 0L); //price is not important since it's the temp item
                if (pp == null) {
                    log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: "
                            + "res was false after create temporary position with item {}",
                            sourceUsrId, targetUsrId, event, targetItemId);
                    return null;
                }
                toDeletePositionId = pp.getPositionId();
                res = updatePretixPositionAction.invoke(event, targetPositionId, new UpdatePretixPositionRequest(
                        targetOrderCode,
                        sourceItemId,
                        sourcePrice
                )) != null;
            } else {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: "
                        + "Creating a new position should not be possible",
                        sourceUsrId, targetUsrId, event);
                PretixPosition pp = pushPretixPositionAction.invoke(event, new PushPretixPositionRequest(
                        targetOrderCode,
                        Objects.requireNonNull(targetAddonToPositionId),
                        sourceItemId
                ), createTempPositionFirst, pretixInformation, sourcePrice);
                res = pp != null;
                if (res) {
                    targetPositionId = pp.getPositionId();
                    sourcePosid = pp.getPositionPosid();
                }
            }
        } else {
            if (targetHasPosition) {
                //res = deletePretixPositionAction.invoke(event, targetPositionId);
                //If we have to delete a position, we delay the deletion after the exchange is done to mantain quota
                toDeletePositionId = targetPositionId;
            }
        }
        if (!res) {
            log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: res was false after C/U/D target",
                    sourceUsrId, targetUsrId, event);
            return null;
        }

        //Update source
        Long targetPosid = null;
        if (targetItemId != null) {
            if (sourceHasPosition) {
                res = updatePretixPositionAction.invoke(event, sourcePositionId, new UpdatePretixPositionRequest(
                        sourceOrderCode,
                        targetItemId,
                        targetPrice
                )) != null;
            } else {
                PretixPosition pp = pushPretixPositionAction.invoke(event, new PushPretixPositionRequest(
                        sourceOrderCode,
                        Objects.requireNonNull(sourceAddonToPositionId),
                        targetItemId
                ), createTempPositionFirst, pretixInformation, targetPrice);
                res = pp != null;
                if (res) {
                    sourcePositionId = pp.getPositionId();
                    targetPosid = pp.getPositionPosid();
                }
            }
        } else {
            if (sourceHasPosition) {
                res = deletePretixPositionAction.invoke(event, sourcePositionId);
            }
        }
        if (!res) {
            log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: res was false after C/U/D source",
                    sourceUsrId, targetUsrId, event);
            return null;
        }

        if (toDeletePositionId != null) {
            res = deletePretixPositionAction.invoke(event, toDeletePositionId);
            if (!res) {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: "
                        + "res was false after deleting toDeletePositionId={}",
                        sourceUsrId, targetUsrId, event, toDeletePositionId);
                return null;
            }
        }

        long sourceBalance = sourcePaid - targetPrice;
        long targetBalance = targetPaid - sourcePrice;

        return new ExchangeResult(
                sourceBalance, targetBalance, sourcePositionId, targetPositionId, sourcePosid, targetPosid
        );
    }
    @Override
    public boolean exchangeRoom(long targetUsrId, long sourceUsrId, long roomId, @NotNull Event event,
                                @NotNull PretixInformation pretixInformation) {
        try {
            OrderController.suspendWebhook();
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
            final String sourceOrderCode = sourceOrder.getCode();
            final String targetOrderCode = targetOrder.getCode();
            final ExtraDays sourceExtraDays = sourceOrder.getExtraDays();
            final ExtraDays targetExtraDays = targetOrder.getExtraDays();

            //Get various items and positions
            final long sourceTicketPosid = sourceOrder.getTicketPositionPosid();
            Long sourceRoomPositionId = sourceOrder.getRoomPositionId();
            Long sourceRoomPosid = sourceOrder.getRoomPositionPosid();
            Long sourceRoomItemId = sourceOrder.getPretixRoomItemId();
            Long sourceEarlyPositionId = null;
            Long sourceLatePositionId = null;
            Long sourceEarlyItemId = null;
            Long sourceLateItemId = null;
            final long targetTicketPosid = targetOrder.getTicketPositionPosid();
            Long targetRoomPositionId = targetOrder.getRoomPositionId();
            Long targetRoomPosid = targetOrder.getRoomPositionPosid();
            Long targetRoomItemId = targetOrder.getPretixRoomItemId();
            Long targetEarlyPositionId = null;
            Long targetLatePositionId = null;
            Long targetEarlyItemId = null;
            Long targetLateItemId = null;

            //Get early and late data
            if (sourceOrder.hasRoom()) {
                HotelCapacityPair sourcePair = new HotelCapacityPair(
                        Objects.requireNonNull(sourceOrder.getHotelInternalName()),
                        Objects.requireNonNull(sourceOrder.getRoomInternalName()),
                        sourceOrder.getRoomCapacity()
                );
                if (sourceExtraDays.isEarly()) {
                    sourceEarlyPositionId = sourceOrder.getEarlyPositionId();
                    sourceEarlyItemId = Objects.requireNonNull(
                            pretixInformation.getExtraDayItemIdForHotelCapacity(sourcePair, ExtraDays.EARLY)
                    );
                }
                if (sourceExtraDays.isLate()) {
                    sourceLatePositionId = sourceOrder.getLatePositionId();
                    sourceLateItemId = Objects.requireNonNull(
                            pretixInformation.getExtraDayItemIdForHotelCapacity(sourcePair, ExtraDays.LATE)
                    );
                }
            }
            if (targetOrder.hasRoom()) {
                HotelCapacityPair targetPair = new HotelCapacityPair(
                        Objects.requireNonNull(targetOrder.getHotelInternalName()),
                        Objects.requireNonNull(targetOrder.getRoomInternalName()),
                        targetOrder.getRoomCapacity()
                );
                if (targetExtraDays.isEarly()) {
                    targetEarlyPositionId = targetOrder.getEarlyPositionId();
                    targetEarlyItemId = Objects.requireNonNull(
                            pretixInformation.getExtraDayItemIdForHotelCapacity(targetPair, ExtraDays.EARLY)
                    );
                }
                if (targetExtraDays.isLate()) {
                    targetLatePositionId = targetOrder.getLatePositionId();
                    targetLateItemId = Objects.requireNonNull(
                            pretixInformation.getExtraDayItemIdForHotelCapacity(targetPair, ExtraDays.LATE)
                    );
                }
            }

            //Run checks on items and positions
            if (targetRoomItemId != null && targetRoomPositionId != null) {
                //If the target user doesn't have a room in his order, default it with NO_ROOM item
                targetRoomItemId = (Long) pretixInformation.getIdsForItemType(CacheItemTypes.NO_ROOM_ITEM).toArray()[0];
            }
            //Source user MUST have a room (we also assume caller has already checked it's not a NO_ROOM)
            if (sourceRoomPositionId == null) {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No sourceRoomPositionId",
                        sourceUsrId, targetUsrId, event);
                return false;
            }
            if (sourceRoomItemId == null) {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No sourceRoomItemId",
                        sourceUsrId, targetUsrId, event);
                return false;
            }

            long sourceBalance = 0L;
            long targetBalance = 0L;

            //Now that we have everything, update pretix and calc balances
            ExchangeResult exchange;
            //Exchange room
            exchange = exchangeSingleItem(
                    sourceRoomItemId, sourceRoomPositionId, targetRoomItemId, targetRoomPositionId,
                    sourceTicketPosid, targetTicketPosid, sourceUsrId, targetUsrId,
                    sourceOrderCode, targetOrderCode, event, pretixInformation, true);
            if (exchange == null) {
                return false;
            }
            sourceRoomPositionId = exchange.sourcePositionId;
            targetRoomPositionId = exchange.targetPositionId;
            sourceRoomPosid = exchange.sourcePosid == null ? sourceRoomPosid : exchange.sourcePosid;
            targetRoomPosid = exchange.targetPosid == null ? targetRoomPosid : exchange.targetPosid;
            sourceBalance += exchange.sourceBalance;
            targetBalance += exchange.targetBalance;
            //Exchange early
            exchange = exchangeSingleItem(
                    sourceEarlyItemId, sourceEarlyPositionId, targetEarlyItemId, targetEarlyPositionId,
                    sourceRoomPosid, targetRoomPosid, sourceUsrId, targetUsrId,
                    sourceOrderCode, targetOrderCode, event, pretixInformation, true);
            if (exchange == null) {
                return false;
            }
            sourceBalance += exchange.sourceBalance;
            targetBalance += exchange.targetBalance;
            //Exchange late
            exchange = exchangeSingleItem(
                    sourceLateItemId, sourceLatePositionId, targetLateItemId, targetLatePositionId,
                    sourceRoomPosid, targetRoomPosid, sourceUsrId, targetUsrId,
                    sourceOrderCode, targetOrderCode, event, pretixInformation, true);
            if (exchange == null) {
                return false;
            }
            sourceBalance += exchange.sourceBalance;
            targetBalance += exchange.targetBalance;

            //If balance > 0 we need to issue a refund, otherwise a payment
            String comment =
                    " was created for a room exchange between orders " + sourceOrderCode + " -> " + targetOrderCode
                    + " happened on timestamp " + System.currentTimeMillis();
            BiFunction<Long, String, Boolean> issuePaymentOrRefund = (balance, orderCode) -> {
                if (balance > 0L) {
                    var balanceForProvider = getBalanceForProvider(orderCode, event);
                    long total = balanceForProvider.values().stream().mapToLong(Long::longValue).sum();
                    balance = Math.min(balance, total);
                    if (balance <= 0L) { //Nothing to be refunded
                        return true;
                    }
                    String amount = PretixGenericUtils.fromPriceToString(balance, '.');
                    return issueRefundAction.invoke(event, orderCode, "Refund" + comment, amount);
                } else if (balance < 0L) {
                    String amount = PretixGenericUtils.fromPriceToString(balance, '.');
                    return issuePaymentAction.invoke(event, orderCode, "Payment" + comment, amount);
                } //Else balance == 0: do nothing, we're done
                return false;
            };
            boolean res = true;
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
                        log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}:"
                                + "Unable to refetch order {} from pretix",
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
        } finally {
            OrderController.resumeWebhook();
        }
    }

    @Override
    public boolean exchangeFullOrder(long targetUsrId, long sourceUsrId, long roomId,
                                     @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        try {
            OrderController.suspendWebhook();
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
            List<PretixPayment> payments = pretixPaymentFinder.getPaymentsForOrder(event, orderCode);
            List<PretixRefund> refunds = pretixRefundFinder.getRefundsForOrder(event, orderCode);
            Map<String, Long> balanceForProvider = getBalanceForProvider(payments, refunds, orderCode, event);

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
        } finally {
            OrderController.resumeWebhook();
        }
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

    private record BuyOrUpgradeResult(
            long effectiveRemaining,
            long positionId,
            @Nullable Long posid,
            @Nullable PretixPosition tempPosition
    ) {}

    private @Nullable BuyOrUpgradeResult buyOrUpgradeItem(
            @Nullable Long originalItemId,
            @Nullable Long newItemId,
            @Nullable Long newItemPrice,
            boolean originallyHadAroomPosition,
            @Nullable Long positionId,
            long addonToPositionId,
            long userId,
            long newRoomItemId,
            @NotNull String orderCode,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation,
            boolean createTempAddonFirst
    ) {
        log.debug("[ROOM_BUY] buyOrUpgradeItem called with "
                + "originalItemId={} newItemId={} newItemPrice={} originallyHadAroomPosition={} "
                + "positionId={} addonToPositionId={} userId={} newRoomItemId={} orderCode={} "
                + "event={} pretixInformation={} createTempAddonFirst={}",
                originalItemId, newItemId, newItemPrice, originallyHadAroomPosition,
                positionId, addonToPositionId, userId, newRoomItemId, orderCode,
                event, pretixInformation, createTempAddonFirst);
        if (newItemId == null || newItemPrice == null) {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: newItemId was null",
                    userId, newRoomItemId, event);
            return null;
        }
        //Fetch quota
        var q = pretixInformation.getSmallestAvailabilityFromItemId(newItemId);
        if (!q.isPresent()) {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: Unable to fetch quota of item {}",
                    userId, newRoomItemId, event, newItemId);
            return null;
        }
        if (!q.get().isAvailable()) {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: There's no available quota of item {}",
                    userId, newRoomItemId, event, newItemId);
            throw new ApiException("New room quota has ended!", RoomErrorCodes.BUY_ROOM_NEW_ROOM_QUOTA_ENDED);
        }

        //If quota is available, try getting the item
        PretixPosition tempPosition = null;
        Long posid = null;
        boolean res;
        if (originallyHadAroomPosition && positionId != null) {
            //Create a temporary position to mantain our quota in case of rollback
            if (originalItemId != null) {
                tempPosition = pushPretixPositionAction.invoke(event, new PushPretixPositionRequest(
                        orderCode,
                        null,
                        originalItemId
                ), createTempAddonFirst, pretixInformation, 0L); //price is not important since it's the temp item
                if (tempPosition == null) {
                    log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}:"
                            + "Unable to create temporary position for rollback with item {}",
                            userId, newRoomItemId, event, originalItemId);
                    return null;
                }
            }
            res = updatePretixPositionAction.invoke(event, positionId, new UpdatePretixPositionRequest(
                    orderCode,
                    newItemId,
                    newItemPrice
            )) != null;
        } else {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: "
                    + "pushing new position should not be possible",
                    userId, newRoomItemId, event);
            PretixPosition pos = pushPretixPositionAction.invoke(event, new PushPretixPositionRequest(
                    orderCode,
                    addonToPositionId,
                    newItemId
            ), createTempAddonFirst, pretixInformation, newItemPrice);
            res = pos != null;
            if (res) {
                positionId = pos.getPositionId();
                posid = pos.getPositionPosid();
            }
        }
        if (!res) {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}:"
                    + "An error occurred while pushing or updating order position {}",
                    userId, newRoomItemId, event, positionId);
            throw new ApiException("Error while adding room to order", RoomErrorCodes.BUY_ROOM_ERROR_UPDATING_POSITION);
        }

        //Refetch how many items are remaining
        var r = pretixInformation.getSmallestAvailabilityFromItemId(newItemId);
        if (!r.isPresent()) {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: Unable to (re)fetch quota of item {}",
                    userId, newRoomItemId, event, newItemId);
            return null;
        }
        return new BuyOrUpgradeResult(r.get().calcEffectiveRemainig(), positionId, posid, tempPosition);
    }
    private void rollbackBuyOrUpgrade(
        @Nullable Long rollbackItemId,
        @Nullable Long rollbackPrice,
        long positionId,
        @NotNull String orderCode,
        boolean originallyHadAroomPosition,
        long userId,
        long newRoomItemId,
        @NotNull Event event
    ) {
        if (rollbackItemId == null || rollbackPrice == null) {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: rollbackItemId was null",
                    userId, newRoomItemId, event);
            return;
        }

        boolean res;
        if (originallyHadAroomPosition) {
            res = updatePretixPositionAction.invoke(event, positionId, new UpdatePretixPositionRequest(
                    orderCode,
                    rollbackItemId,
                    rollbackPrice
            )) != null;
        } else {
            res = deletePretixPositionAction.invoke(event, positionId);
        }
        if (!res) {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}:"
                    + "An error occurred while trying to removing room after a race condition was detected",
                    userId, newRoomItemId, event);
        }
    }
    private void finalizeBuyOrUpgrade(@NotNull Event event, long userId, long newRoomItemId,
                                      BuyOrUpgradeResult... results) {
        //Delete the temporary positions created for mantain our quota
        for (BuyOrUpgradeResult result : results) {
            if (result != null && result.tempPosition != null) {
                boolean res = deletePretixPositionAction.invoke(event, result.tempPosition.getPositionId());
                if (!res) {
                    log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}:"
                            + "An error occurred while finalizing position p{} t{}",
                            userId, newRoomItemId, event, result.positionId, result.tempPosition.getPositionId());
                }
            }
        }
    }
    @Override
    public synchronized boolean buyOrUpgradeRoom(
            long newRoomItemId, long newRoomPrice, @Nullable Long oldRoomPaid,
            long userId,
            @Nullable Long roomId,
            @Nullable Long newEarlyItemId, @Nullable Long newEarlyPrice, @Nullable Long oldEarlyPaid,
            @Nullable Long newLateItemId, @Nullable Long newLatePrice, @Nullable Long oldLatePaid,
            @NotNull Order order,
            @NotNull Event event,
            @NotNull PretixInformation pretixInformation
    ) {
        try {
            OrderController.suspendWebhook();
            log.info("[ROOM_BUY] User {} buying roomItemId {} on event {}:"
                            + "User is buying or upgrading his room to r{} e{} l{}",
                    userId, newRoomItemId, event, newRoomItemId, newEarlyItemId, newLateItemId);
            log.debug("[ROOM_BUY] buyOrUpgradeRoom called with params: "
                + "newRoomItemId={} newRoomPrice={} oldRoomPaid={} userId={} roomId={} "
                + "newEarlyItemId={} newEarlyPrice={} oldEarlyPaid={} "
                + "newLateItemId={} newLatePrice={} oldLatePaid={} "
                + "order=({}) evet={} pretixInformation={}",
                newRoomItemId, newRoomPrice, oldRoomPaid, userId, roomId,
                newEarlyItemId, newEarlyPrice, oldEarlyPaid,
                newLateItemId, newLatePrice, oldLatePaid,
                order.toFullString(), event, pretixInformation
            );
            Long roomPositionId = order.getRoomPositionId();
            final boolean originallyHadAroomPosition = roomPositionId != null;
            Long earlyPositionId = order.getEarlyPositionId();
            Long latePositionId = order.getLatePositionId();
            final String orderCode = order.getCode();
            final ExtraDays extraDays = order.getExtraDays();

            //Fetch the original item ids for rolling back
            //Room
            Long originalRoomItemId = order.getPretixRoomItemId();
            if (originalRoomItemId == null) {
                //If order has no room, we fall back to NO_ROOM item
                originalRoomItemId = (Long)
                        pretixInformation.getIdsForItemType(CacheItemTypes.NO_ROOM_ITEM).toArray()[0];
            }
            //Extra days
            Long originalEarlyItemId = null;
            Long originalLateItemId = null;
            if (order.hasRoom()) {
                HotelCapacityPair originalPair = new HotelCapacityPair(
                        Objects.requireNonNull(order.getHotelInternalName()),
                        Objects.requireNonNull(order.getRoomInternalName()),
                        order.getRoomCapacity()
                );
                if (extraDays.isEarly()) {
                    originalEarlyItemId = Objects.requireNonNull(
                            pretixInformation.getExtraDayItemIdForHotelCapacity(originalPair, ExtraDays.EARLY)
                    );
                }
                if (extraDays.isLate()) {
                    originalLateItemId = Objects.requireNonNull(
                            pretixInformation.getExtraDayItemIdForHotelCapacity(originalPair, ExtraDays.LATE)
                    );
                }
            }

            //Perform the upgrades
            BuyOrUpgradeResult roomRes = buyOrUpgradeItem(originalRoomItemId, newRoomItemId, newRoomPrice,
                    originallyHadAroomPosition, roomPositionId,
                    order.getTicketPositionPosid(), userId, newRoomItemId,
                    orderCode, event, pretixInformation, true);
            if (roomRes == null) {
                return false;
            }
            roomPositionId = roomRes.positionId;
            long roomPosid = roomRes.posid == null
                    ? Objects.requireNonNull(order.getRoomPositionPosid())
                    : roomRes.posid;
            long roomRemaining = roomRes.effectiveRemaining;
            long earlyRemaining = Long.MAX_VALUE;
            long lateRemaining = Long.MAX_VALUE;
            BuyOrUpgradeResult earlyRes = null;
            BuyOrUpgradeResult lateRes = null;
            if (order.hasRoom()) {
                if (extraDays.isEarly()) {
                    earlyRes = buyOrUpgradeItem(originalEarlyItemId, newEarlyItemId, newEarlyPrice,
                            originallyHadAroomPosition, earlyPositionId,
                            roomPosid, userId, newRoomItemId,
                            orderCode, event, pretixInformation, true);
                    if (earlyRes == null) {
                        return false;
                    }
                    earlyPositionId = earlyRes.positionId;
                    earlyRemaining = earlyRes.effectiveRemaining;
                }
                if (extraDays.isLate()) {
                    lateRes = buyOrUpgradeItem(originalLateItemId, newLateItemId, newLatePrice,
                            originallyHadAroomPosition, latePositionId,
                            roomPosid, userId, newRoomItemId,
                            orderCode, event, pretixInformation, true);
                    if (lateRes == null) {
                        return false;
                    }
                    latePositionId = lateRes.positionId;
                    lateRemaining = lateRes.effectiveRemaining;
                }
            }


            //If one of the quota has a negative value, revert back
            if (roomRemaining < 0L || earlyRemaining < 0L || lateRemaining < 0L) {
                //Rollback in reverse order because we need to delete addon firsts
                if (order.hasRoom()) {
                    if (extraDays.isEarly() && earlyPositionId != null) {
                        rollbackBuyOrUpgrade(originalEarlyItemId, oldEarlyPaid, earlyPositionId,
                                orderCode, originallyHadAroomPosition, userId, newRoomItemId, event);
                    }
                    if (extraDays.isLate() && latePositionId != null) {
                        rollbackBuyOrUpgrade(originalLateItemId, oldLatePaid, latePositionId,
                                orderCode, originallyHadAroomPosition, userId, newRoomItemId, event);
                    }
                }
                rollbackBuyOrUpgrade(originalRoomItemId, oldRoomPaid, roomPositionId,
                        orderCode, originallyHadAroomPosition, userId, newRoomItemId, event);
                log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: "
                                + "(RACE CONDITION DETECTED) Quota was available before updating the position, "
                                + "but it was found negative (r{}; e{}; l{}) negative after",
                        userId, newRoomItemId, event, roomRemaining, earlyRemaining, lateRemaining);
                finalizeBuyOrUpgrade(event, userId, newRoomItemId, roomRes, earlyRes, lateRes);
                throw new ApiException(
                        "New room quota has ended (RACE CONDITION DETECTED)",
                        RoomErrorCodes.BUY_ROOM_NEW_ROOM_QUOTA_ENDED
                );
            }
            finalizeBuyOrUpgrade(event, userId, newRoomItemId, roomRes, earlyRes, lateRes);

            //If everything's ok, refetch updated order from pretix
            var pair = event.getOrganizerAndEventPair();
            var refetchedOrder = pretixOrderFinder.fetchOrderByCode(pair.getOrganizer(), pair.getEvent(), orderCode);
            if (!refetchedOrder.isPresent()) {
                log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: Unable to refetch order {} from pretix",
                        userId, newRoomItemId, event, orderCode);
                return false;
            }
            return updateOrderInDb.execute(refetchedOrder.get(), event, pretixInformation).isPresent();
        } finally {
            OrderController.resumeWebhook();
        }
    }

    private void sanityCheckLogAndStoreErrors(@Nullable List<String> logbook, String message, Object... args) {
        log.error(message, args);
        if (logbook != null) {
            logbook.add(message);
        }
    }

    private void sendSanityCheckMailRoomDeleted(long roomId, @NotNull String roomName, @NotNull String reason) {
        mailService.broadcastProblem(
                roomId, TEMPLATE_SANITY_CHECK_DELETED,
                MailVarPair.of(ROOM_NAME, roomName),
                MailVarPair.of(SANITY_CHECK_REASON, reason)
        );
    }
    private void sendSanityCheckMailUserKicked(long ownerId, long userId, @NotNull String fursonaName,
                                               @NotNull String roomName, @NotNull String reason) {
        mailService.sendProblem(
                ownerId, TEMPLATE_SANITY_CHECK_KICK_OWNER,
                MailVarPair.of(OTHER_FURSONA_NAME, fursonaName),
                MailVarPair.of(ROOM_NAME, roomName),
                MailVarPair.of(SANITY_CHECK_REASON, reason)
        );
        mailService.sendProblem(
                userId, TEMPLATE_SANITY_CHECK_KICK_USER,
                MailVarPair.of(ROOM_NAME, roomName),
                MailVarPair.of(SANITY_CHECK_REASON, reason)
        );
    }
    @Override
    public void doSanityChecks(long roomId, @NotNull PretixInformation pretixInformation,
                               @Nullable List<String> detectedErrors) {
        log.info("[ROOM SANITY CHECKS] Running room sanity check on room {}", roomId);
        final Event event = pretixInformation.getCurrentEvent();
        final long eventId = event.getId();
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
        final List<RoomGuestResponse> guests = roomFinder.getRoomGuestResponseFromRoomId(roomId, event);
        final List<RoomGuestResponse> confirmedGuests =
                guests.stream().filter(k -> k.getRoomGuest().isConfirmed()).toList();

        String roomName = roomFinder.getRoomName(roomId);
        roomName = roomName == null ? "" : roomName;

        var ownerIdOpt = roomFinder.getOwnerUserIdFromRoomId(roomId);
        if (!ownerIdOpt.isPresent()) {
            //delete room, no owner found
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "No owner found in room {}. Deleting the room", roomId);
            sendSanityCheckMailRoomDeleted(roomId, roomName, SC_NO_OWNER);
            this.deleteRoom(roomId);
            return;
        }
        long ownerId = ownerIdOpt.get();
        Order order = orderFinder.findOrderByUserIdEvent(ownerId, event, pretixInformation);
        if (order == null) {
            //delete room, owner has no order
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "Owner {} of room {} has no order. Deleting the room", ownerId, roomId);
            sendSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_HAS_NO_ORDER);
            this.deleteRoom(roomId);
            return;
        }
        if (!order.hasRoom()) {
            //delete room, owner has no room
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "Owner {} of room {} hasn't bought a room. Deleting the room", ownerId, roomId);
            sendSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_HAS_NOT_BOUGHT_ROOM);
            this.deleteRoom(roomId);
            return;
        }
        if (order.isDaily()) {
            //delete room, owner has daily ticket
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "Owner {} of room {} has a daily ticket. Deleting the room", ownerId, roomId);
            sendSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_HAS_DAILY_TICKET);
            this.deleteRoom(roomId);
            return;
        }

        int guestNo = confirmedGuests.size();
        int capacity = (int) order.getRoomCapacity();
        if (guestNo > capacity) {
            //delete room, too many members!
            sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                    + "Room {} has too many members ({} > {}). Deleting the room", roomId, guestNo, capacity);
            sendSanityCheckMailRoomDeleted(roomId, roomName, SC_TOO_MANY_MEMBERS);
            this.deleteRoom(roomId);
            return;
        }

        boolean ownerFound = false;
        for (RoomGuestResponse guest : confirmedGuests) {
            long usrId = guest.getUser().getUserId();
            long guestId = guest.getRoomGuest().getGuestId();

            UserEmailData mail = userFinder.getMailDataForUser(usrId);
            String fursona = mail == null ? null : mail.getFursonaName();
            fursona = fursona == null ? "" : fursona;

            //This (together with owner in room check) will also test if a owner has multiple rooms
            int roomNo = roomFinder.countRoomsWithUser(usrId, eventId);
            if (roomNo > 1) {
                if (usrId == ownerId) {
                    //delete room, owner is in too many rooms
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "Owner {} of room {} is in too many rooms ({})!. Deleting the room", usrId, roomId, roomNo);
                    sendSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_IN_TOO_MANY_ROOMS);
                    this.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, he's in too many rooms
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "User {}g{} of room {} is in too many rooms ({})!. Kicking the user",
                        usrId, guestId, roomId, roomNo);
                    sendSanityCheckMailUserKicked(ownerId, usrId, fursona, roomName, SC_USER_IN_TOO_MANY_ROOMS);
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
                    sendSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_ORDER_INVALID_STATUS);
                    this.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, his order is canceled
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "User {}g{} of room {} has a canceled order!. Kicking the user", usrId, guestId, roomId);
                    sendSanityCheckMailUserKicked(ownerId, usrId, fursona, roomName,
                            SC_USER_ORDER_INVALID_ORDER_STATUS);
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
                    sendSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_HAS_NO_ORDER);
                    this.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, user has no order
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "User {}g{} of room {} has no order. Kicking the user", usrId, guestId, roomId);
                    sendSanityCheckMailUserKicked(ownerId, usrId, fursona, roomName, SC_USER_HAS_NO_ORDER);
                    this.kickFromRoom(guestId);
                    continue;
                }
            }
            if (daily.get()) {
                if (usrId == ownerId) {
                    //delete room, owner has a daily ticket
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "Owner {} of room {} has a daily ticket. Deleting the room", usrId, roomId);
                    sendSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_HAS_DAILY_TICKET);
                    this.deleteRoom(roomId);
                    return;
                } else {
                    //kick user, he has a daily ticket
                    sanityCheckLogAndStoreErrors(detectedErrors, "[ROOM SANITY CHECKS] "
                        + "User {}g{} of room {} has a daily ticket. Kicking the user", usrId, guestId, roomId);
                    sendSanityCheckMailUserKicked(ownerId, usrId, fursona, roomName, SC_USER_HAS_DAILY_TICKET);
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
            sendSanityCheckMailRoomDeleted(roomId, roomName, SC_OWNER_NOT_IN_ROOM);
            this.deleteRoom(roomId);
            return;
        }
    }
}
