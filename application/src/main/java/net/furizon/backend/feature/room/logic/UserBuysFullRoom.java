package net.furizon.backend.feature.room.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.nosecount.dto.NosecountRoom;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.action.changeOrder.ChangeOrderAction;
import net.furizon.backend.feature.pretix.objects.order.action.deletePosition.DeletePretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.action.pushAnswer.PushPretixAnswerAction;
import net.furizon.backend.feature.pretix.objects.order.action.pushPosition.PushPretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.action.updatePosition.UpdatePretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.backend.feature.pretix.objects.order.dto.request.ChangeOrderRequest;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixBalanceForProviderFinder;
import net.furizon.backend.feature.pretix.objects.payment.action.yeetPayment.IssuePaymentAction;
import net.furizon.backend.feature.pretix.objects.order.dto.request.PushPretixPositionRequest;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.feature.pretix.objects.order.usecase.UpdateOrderInDb;
import net.furizon.backend.feature.pretix.objects.payment.finder.PretixPaymentFinder;
import net.furizon.backend.feature.pretix.objects.quota.QuotaException;
import net.furizon.backend.feature.pretix.objects.refund.finder.PretixRefundFinder;
import net.furizon.backend.feature.room.RoomGeneralSanityCheck;
import net.furizon.backend.feature.room.action.exchangeRoom.ExchangeRoomOnPretixAction;
import net.furizon.backend.feature.room.action.transferOrder.TransferPretixOrderAction;
import net.furizon.backend.feature.room.dto.RoomData;
import net.furizon.backend.feature.room.dto.RoomErrorCodes;
import net.furizon.backend.feature.room.finder.RoomFinder;
import net.furizon.backend.feature.room.RoomChecks;
import net.furizon.backend.feature.user.dto.UserDisplayDataWithExtraDays;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.localization.model.TranslatableValue;
import net.furizon.backend.infrastructure.pretix.PretixConst;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.GeneralResponseCodes;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static net.furizon.backend.infrastructure.email.EmailVars.ORDER_CODE;
import static net.furizon.backend.infrastructure.email.EmailVars.REFUND_MONEY;
import static net.furizon.backend.infrastructure.pretix.PretixGenericUtils.PRETIX_DATETIME_FORMAT;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_UPGRADE_FAILED_POSITION;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_UPGRADE_FAILED_PRICE;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "room", name = "logic", havingValue = "roomLogic-user-buys-full-room")
public class UserBuysFullRoom implements RoomLogic {
    @NotNull private final RoomChecks checks;
    @NotNull private final UpdateOrderInDb updateOrderInDb;
    @NotNull private final DefaultRoomLogic defaultRoomLogic;
    @NotNull private final RoomGeneralSanityCheck sanityChecks;
    @NotNull private final PretixOrderFinder pretixOrderFinder;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final RoomFinder roomFinder;
    @NotNull private final EmailSender emailSender;
    @NotNull private final TranslationService translationService;


    //Buy or upgrade related stuff
    @NotNull private final ChangeOrderAction changeOrderAction;
    @NotNull private final DeletePretixPositionAction deletePretixPositionAction;

    //Transfer full order related stuff
    @NotNull private final TransferPretixOrderAction transferOrderAction;
    @NotNull private final PretixRefundFinder pretixRefundFinder;
    @NotNull private final PretixPaymentFinder pretixPaymentFinder;
    @NotNull private final PushPretixAnswerAction pushPretixAnswerAction;
    @NotNull private final PretixBalanceForProviderFinder pretixBalanceForProviderFinder;

    //Exchange room related stuff
    @NotNull private final ExchangeRoomOnPretixAction exchangeRoomAction;
    @NotNull private final PushPretixPositionAction pushPretixPositionAction;
    @NotNull private final UpdatePretixPositionAction updatePretixPositionAction;
    @NotNull private final IssuePaymentAction issuePaymentAction;

    @Override
    public boolean canCreateRoom(long userId, @NotNull Event event) {
        var r = orderFinder.userHasBoughtAroom(userId, event);
        return r.isPresent() && r.get() && defaultRoomLogic.canCreateRoom(userId, event);
    }

    @Override
    public long createRoom(String name, long userId,
                           @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        checks.assertUserHasBoughtAroom(userId, event);
        return defaultRoomLogic.createRoom(name, userId, event, pretixInformation);
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
    public boolean setShowInNosecount(boolean showInNosecount, long roomId) {
        return defaultRoomLogic.setShowInNosecount(showInNosecount, roomId);
    }

    @Override
    public long invitePersonToRoom(long invitedUserId, long roomId,
                                   @NotNull Event event, @NotNull PretixInformation pretixInformation,
                                   boolean force, boolean forceExit) {
        checks.assertRoomNotFull(roomId, true);
        checks.assertUserHasNotBoughtAroom(invitedUserId, event);
        return defaultRoomLogic.invitePersonToRoom(invitedUserId, roomId, event, pretixInformation, force, forceExit);
    }

    @Override
    public boolean inviteAccept(long guestId, long invitedUserId, long roomId,
                                @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        checks.assertRoomNotFull(roomId, true);
        checks.assertUserHasNotBoughtAroom(invitedUserId, event);
        return defaultRoomLogic.inviteAccept(guestId, invitedUserId, roomId, event, pretixInformation);
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
    public boolean canConfirmRoom(long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        return false;
    }

    @Override
    public boolean canUnconfirmRoom(long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        return false;
    }

    @Override
    public boolean confirmRoom(long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        return false;
    }

    @Override
    public boolean isUnconfirmationSupported() {
        return false;
    }

    @Override
    public boolean unconfirmRoom(long roomId, @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        return false;
    }

    @Override
    public boolean isExchangeRoomSupported(@NotNull Event event) {
        return true;
    }

    @Override
    public boolean isExchangeFullOrderSupported(@NotNull Event event) {
        return true;
    }

    @Override
    @Transactional
    public boolean exchangeRoom(long targetUsrId, long sourceUsrId,
                                @Nullable Long targetRoomId, @Nullable Long sourceRoomId,
                                @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.debug("[ROOM_EXCHANGE] called with params: "
                + "targetUsrId={} sourceUsrId={} targetRoomId={} sourceRoomId={} event={} pretixInformation={}",
                targetUsrId, sourceUsrId, targetRoomId, sourceRoomId, event, pretixInformation);
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

            //Get positions
            Long sourceRoomPositionId = sourceOrder.getRoomPositionId();
            Long sourceEarlyPositionId = null;
            Long sourceLatePositionId = null;
            Long targetRoomPositionId = targetOrder.getRoomPositionId();
            Long targetEarlyPositionId = null;
            Long targetLatePositionId = null;

            //Get early and late data
            if (sourceOrder.hasRoom()) {
                if (sourceExtraDays.isEarly()) {
                    sourceEarlyPositionId = sourceOrder.getEarlyPositionId();
                }
                if (sourceExtraDays.isLate()) {
                    sourceLatePositionId = sourceOrder.getLatePositionId();
                }
            }
            if (targetOrder.hasRoom()) {
                if (targetExtraDays.isEarly()) {
                    targetEarlyPositionId = targetOrder.getEarlyPositionId();
                }
                if (targetExtraDays.isLate()) {
                    targetLatePositionId = targetOrder.getLatePositionId();
                }
            }

            //Run checks on positions
            //Both source and target MUST have a room. Reminder that without room they have NO_ROOM item
            if (sourceRoomPositionId == null) {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No sourceRoomPositionId",
                        sourceUsrId, targetUsrId, event);
                return false;
            }
            if (targetRoomPositionId == null) {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: No targetRoomPositionId",
                        sourceUsrId, targetUsrId, event);
                return false;
            }


            log.debug("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: "
                    + "Loaded params: sourceOrder=({}) targetOrder=({})",
                    sourceUsrId, targetUsrId, event, sourceOrder.toFullString(), targetOrder.toFullString());

            final String comment =
                      " was created for a room exchange between orders " + sourceOrderCode + " -> " + targetOrderCode
                    + " happened on " + PRETIX_DATETIME_FORMAT.format(LocalDate.now());
            final String paymentComment = "Payment" + comment + ". DO NOT REFUND ANY PAYMENT FROM THIS ORDER!";
            final String refundComment = "Refund" + comment;

            //Changes in DB
            boolean dbRes = defaultRoomLogic.exchangeRoom(
                    targetUsrId, sourceUsrId, targetRoomId, sourceRoomId, event, pretixInformation);
            if (!dbRes) {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: Database update returned false!",
                        sourceUsrId, targetUsrId, event);
                throw new ApiException(translationService.error("common.server_error"),
                        GeneralResponseCodes.GENERIC_ERROR);
            }

            //Exchange rooms on pretix
            boolean pretixRes = exchangeRoomAction.invoke(
                sourceOrderCode,
                sourceRoomPositionId,
                sourceEarlyPositionId,
                sourceLatePositionId,

                targetOrderCode,
                targetRoomPositionId,
                targetEarlyPositionId,
                targetLatePositionId,

                paymentComment,
                refundComment,
                event
            );
            if (!pretixRes) {
                log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: Pretix update returned false!",
                        sourceUsrId, targetUsrId, event);
                //We're in a @Transactional. Throwing an exception makes it rollback database changes
                throw new ApiException(translationService.error("common.server_error"),
                        GeneralResponseCodes.GENERIC_ERROR);
            }

            //Refresh order
            var pair = event.getOrganizerAndEventPair();
            String eventName = pair.getEvent();
            String organizerName = pair.getOrganizer();
            Function<String, Boolean> fetchAndUpdateOrder = (orderCode) -> {
                var order = pretixOrderFinder.fetchOrderByCode(organizerName, eventName, orderCode);
                if (!order.isPresent()) {
                    log.error("[ROOM_EXCHANGE] Exchange {} -> {} on event {}: "
                            + "Unable to refetch order {} from pretix",
                            sourceUsrId, targetUsrId, event, orderCode);
                    return false;
                }
                return updateOrderInDb.execute(order.get(), event, pretixInformation).isPresent();
            };

            //Fully reload orders from pretix to the DB
            boolean res = fetchAndUpdateOrder.apply(targetOrderCode);
            defaultRoomLogic.logExchangeError(res, 106, targetUsrId, sourceUsrId, event);
            res = res && fetchAndUpdateOrder.apply(sourceOrderCode);
            defaultRoomLogic.logExchangeError(res, 107, targetUsrId, sourceUsrId, event);

            return res;
        } finally {
            OrderController.resumeWebhook();
        }
    }

    @Override
    @Transactional
    public boolean exchangeFullOrder(long targetUsrId, long sourceUsrId, long roomId,
                                     @NotNull Event event, @NotNull PretixInformation pretixInformation) {
        log.debug("[ORDER_TRANSFER] exchangeFullOrder called with params:"
                + "targetUsrId={} sourceUsrId={} roomId={} event={} pretixInformation={}",
                targetUsrId, sourceUsrId, roomId, event, pretixInformation);
        try {
            OrderController.suspendWebhook();
            log.info("[ORDER_TRANSFER] UserBuysFullRoom: Transferring order {} -> {} on event {}",
                    sourceUsrId, targetUsrId, event);

            //Fetch source order
            Order sourceOrder = orderFinder.findOrderByUserIdEvent(sourceUsrId, event, pretixInformation);
            if (sourceOrder == null) {
                log.error("[ORDER_TRANSFER] {} -> {} on event {}: Unable to find order for user {}",
                        sourceUsrId, targetUsrId, event, sourceUsrId);
                return false;
            }
            //Fetch extra data
            String orderCode = sourceOrder.getCode();
            long positionId = sourceOrder.getTicketPositionId();
            var questionId = pretixInformation.getQuestionIdFromIdentifier(PretixConst.QUESTIONS_ACCOUNT_USERID);
            if (questionId.isEmpty()) {
                log.error("[ORDER_TRANSFER] {} -> {} on event {}: Unable to find userId question",
                        sourceUsrId, targetUsrId, event);
                return false;
            }

            final String comment =
                  " was created for an order exchange between users " + sourceUsrId + " -> " + targetUsrId
                + " happened on " + PRETIX_DATETIME_FORMAT.format(LocalDate.now());
            final String paymentComment = "Payment" + comment + ". DO NOT REFUND ANY PAYMENT FROM THIS ORDER!";
            final String refundComment = "Refund" + comment;

            //Changes in DB
            boolean dbRes = defaultRoomLogic.exchangeFullOrder(
                    targetUsrId,
                    sourceUsrId,
                    roomId,
                    event,
                    pretixInformation
            );
            if (!dbRes) {
                log.error("[ORDER_TRANSFER] {} -> {} on event {}: Database update returned false!",
                        sourceUsrId, targetUsrId, event);
                throw new ApiException(translationService.error("common.server_error"),
                        GeneralResponseCodes.GENERIC_ERROR);
            }

            //Changes on pretix
            boolean pretixRes = transferOrderAction.invoke(
                orderCode,
                positionId,
                questionId.get(),
                targetUsrId,

                paymentComment,
                refundComment,

                event
            );
            if (!pretixRes) {
                log.error("[ORDER_TRANSFER] {} -> {} on event {}: Pretix update returned false!",
                        sourceUsrId, targetUsrId, event);
                //We're in a @Transactional. Throwing an exception makes it rollback database changes
                throw new ApiException(translationService.error("common.server_error"),
                        GeneralResponseCodes.GENERIC_ERROR);
            }

            //Refetch pretix order and update it in db
            var pair = event.getOrganizerAndEventPair();
            var order = pretixOrderFinder.fetchOrderByCode(pair.getOrganizer(), pair.getEvent(), orderCode);
            if (!order.isPresent()) {
                log.error("[ORDER_TRANSFER] {} -> {} on event {}: Unable to refetch order {} from pretix",
                        sourceUsrId, targetUsrId, event, orderCode);
                return false;
            }
            boolean res = updateOrderInDb.execute(order.get(), event, pretixInformation).isPresent();
            defaultRoomLogic.logExchangeError(res, 203, targetUsrId, sourceUsrId, event);

            return res;
        } finally {
            OrderController.resumeWebhook();
        }
    }

    @Override
    public boolean isRefundRoomSupported(@NotNull Event event) {
        return false;
    }

    @Override
    public boolean isRefundFullOrderSupported(@NotNull Event event) {
        return false;
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
    public boolean buyOrUpgradeRoom(
            long newRoomItemId, long newRoomPrice, @Nullable Long oldRoomPaid,
            long userId,
            @Nullable Long roomId,
            @Nullable Long newEarlyItemId, @Nullable Long newEarlyPrice, @Nullable Long oldEarlyPaid,
            @Nullable Long newLateItemId, @Nullable Long newLatePrice, @Nullable Long oldLatePaid,
            boolean disablePriceUpgradeChecks,
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
            final Long earlyPositionId = order.getEarlyPositionId();
            final Long latePositionId = order.getLatePositionId();
            final String orderCode = order.getCode();
            final ExtraDays extraDays = order.getExtraDays();

            ChangeOrderRequest req = new ChangeOrderRequest();

            //We can deal with price toctou race conditions, if an admin changes the prices after we got them

            // Set up room item
            if (originallyHadAroomPosition) {
                req.patchPosition(
                    roomPositionId,
                    ChangeOrderRequest.PatchPosition.builder()
                            .item(newRoomItemId)
                        .build()
                        .setPrice(newRoomPrice)
                );
            } else {
                req.createPosition(
                    PushPretixPositionRequest.builder()
                            .orderCode(orderCode)
                            .addonTo(order.getTicketPositionPosid()) //Should not be needed
                            .item(newRoomItemId)
                        .build()
                        .setPrice(newRoomPrice)
                );
            }

            // Set up extra days
            if (order.hasRoom()) {
                if (extraDays.isEarly() && earlyPositionId != null && newEarlyItemId != null && newEarlyPrice != null) {
                    req.patchPosition(
                        earlyPositionId,
                        ChangeOrderRequest.PatchPosition.builder()
                                .item(newEarlyItemId)
                            .build()
                            .setPrice(newEarlyPrice)
                    );
                }
                if (extraDays.isLate() && latePositionId != null && newLateItemId != null && newLatePrice != null) {
                    req.patchPosition(
                        latePositionId,
                        ChangeOrderRequest.PatchPosition.builder()
                                .item(newLateItemId)
                            .build()
                            .setPrice(newLatePrice)
                    );
                }
            }

            // Do the actual request
            boolean changeResult;
            try {
                changeResult = changeOrderAction.invoke(req, orderCode, event);
            } catch (QuotaException q) {
                log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: "
                        + "A quota error happened while changing order {}. Details: {}",
                        userId, newRoomItemId, event, orderCode, q.getMessage());
                throw new ApiException(translationService.error("room.buy.fail.quota_ended"),
                        RoomErrorCodes.BUY_ROOM_NEW_ROOM_QUOTA_ENDED);
            }

            if (!changeResult) {
                log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: "
                        + "ChangeOrderAction returned false while working on order {}",
                        userId, newRoomItemId, event, orderCode);
                return false;
            }

            //If pretix update is ok, refetch updated order from pretix
            var pair = event.getOrganizerAndEventPair();
            var refetchedOrder = pretixOrderFinder.fetchOrderByCode(pair.getOrganizer(), pair.getEvent(), orderCode);
            if (!refetchedOrder.isPresent()) {
                log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: Unable to refetch order {} from pretix",
                        userId, newRoomItemId, event, orderCode);
                return false;
            }
            PretixOrder pretixOrder = refetchedOrder.get();
            var refreshedOrderOpt = updateOrderInDb.execute(pretixOrder, event, pretixInformation);
            boolean updateResult = refreshedOrderOpt.isPresent();

            if (updateResult) {
                //There's a small TOCTOU race condition window with both price and early/late items.
                // Recheck that everything is actually ok

                Order refreshedOrder =  refreshedOrderOpt.get();
                long totalPaid = (oldRoomPaid == null ? 0L : oldRoomPaid)
                               + (oldEarlyPaid == null ? 0L : oldEarlyPaid)
                               + (oldLatePaid  == null ? 0L : oldLatePaid);
                if (!originallyHadAroomPosition) {
                    roomPositionId = refreshedOrder.getRoomPositionId();
                }
                long totalPrice = 0L;
                boolean foundPositionIncoherence = false;

                //Obtain price and check for different items
                for (PretixPosition pos : pretixOrder.getPositions()) {
                    if (roomPositionId != null && pos.getPositionId() == roomPositionId) {
                        totalPrice += PretixGenericUtils.fromStrPriceToLong(pos.getPrice());
                        foundPositionIncoherence |= pos.getItemId() != newRoomItemId;
                    }
                    if (earlyPositionId != null && newEarlyItemId != null && pos.getPositionId() == earlyPositionId) {
                        totalPrice += PretixGenericUtils.fromStrPriceToLong(pos.getPrice());
                        foundPositionIncoherence |= pos.getItemId() != newEarlyItemId;
                    }
                    if (latePositionId != null && newLateItemId != null && pos.getPositionId() == latePositionId) {
                        totalPrice += PretixGenericUtils.fromStrPriceToLong(pos.getPrice());
                        foundPositionIncoherence |= pos.getItemId() != newLateItemId;
                    }
                }
                // Check if now we have an early/late position and previously we had not
                foundPositionIncoherence |= refreshedOrder.getEarlyPositionId() != null && newEarlyItemId == null;
                foundPositionIncoherence |= refreshedOrder.getLatePositionId() != null && newLateItemId == null;

                if (totalPaid < totalPrice && !disablePriceUpgradeChecks) {
                    log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: Order {}:"
                            + "Current price ({}) is less than what was previously paid ({})",
                            userId, newRoomItemId, event, orderCode, totalPrice, totalPaid);
                    emailSender.prepareAndSendForPermission(
                        Permission.PRETIX_ADMIN,
                        TranslatableValue.ofEmail("mail.room_upgade_failed_price.title"),
                        TEMPLATE_UPGRADE_FAILED_PRICE,
                        MailVarPair.of(ORDER_CODE, orderCode),
                        MailVarPair.of(REFUND_MONEY, totalPaid + " < " + totalPrice)
                    );
                }
                if (foundPositionIncoherence) {
                    log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: "
                            + "Found some position incoherence. "
                            + "OriginalOrder=({})    RefetchedOrder=({})    RefreshedOrder=({})",
                            userId, newRoomItemId, event, order, refetchedOrder, refreshedOrder);
                    emailSender.prepareAndSendForPermission(
                            Permission.PRETIX_ADMIN,
                            TranslatableValue.ofEmail("mail.room_upgade_failed_position.title"),
                            TEMPLATE_UPGRADE_FAILED_POSITION,
                            MailVarPair.of(ORDER_CODE, orderCode)
                    );
                }
            }

            return updateResult;
        } finally {
            OrderController.resumeWebhook();
        }
    }

    @Override
    public @Nullable ExtraDays getExtraDaysForUser(long userId, long eventId) {
        return roomFinder.getExtraDaysOfRoomOwner(userId, eventId);
    }

    @Override
    public void computeNosecountExtraDays(@NotNull NosecountRoom room) {
        List<UserDisplayDataWithExtraDays> guests = room.getGuests();
        //Since the room extra days will depend on the owner, and it's already set by the caller,
        // we can just use it
        ExtraDays ownerExtraDays = Objects.requireNonNull(room.getRoomExtraDays());
        guests.forEach(g -> g.setExtraDays(ownerExtraDays));
    }

    @Override
    public void updateRoomCapacity(@NotNull RoomData roomData,
                                   @NotNull Event event, @NotNull PretixInformation pretixInformation) {
    }

    @Override
    public void doSanityChecks(long roomId,
                               @NotNull PretixInformation pretixInformation, @Nullable List<String> detectedErrors) {
        sanityChecks.doSanityChecks(
                roomId,
                this,
                pretixInformation,
                detectedErrors
        );
    }
}
