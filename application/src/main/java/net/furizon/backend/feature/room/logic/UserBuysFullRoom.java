package net.furizon.backend.feature.room.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.nosecount.dto.NosecountRoom;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixPosition;
import net.furizon.backend.feature.pretix.objects.order.action.deletePosition.DeletePretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.action.pushAnswer.PushPretixAnswerAction;
import net.furizon.backend.feature.pretix.objects.order.action.pushPosition.PushPretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.action.updatePosition.UpdatePretixPositionAction;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixBalanceForProviderFinder;
import net.furizon.backend.feature.pretix.objects.payment.action.yeetPayment.IssuePaymentAction;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.pretix.objects.order.dto.PushPretixPositionRequest;
import net.furizon.backend.feature.pretix.objects.order.dto.UpdatePretixPositionRequest;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.feature.pretix.objects.order.usecase.UpdateOrderInDb;
import net.furizon.backend.feature.pretix.objects.payment.finder.PretixPaymentFinder;
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
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static net.furizon.backend.infrastructure.email.EmailVars.FURSONA_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.ORDER_CODE;
import static net.furizon.backend.infrastructure.email.EmailVars.OTHER_FURSONA_NAME;
import static net.furizon.backend.infrastructure.email.EmailVars.OTHER_ORDER_CODE;
import static net.furizon.backend.infrastructure.pretix.PretixGenericUtils.PRETIX_DATETIME_FORMAT;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_EXCHANGE_FULLORDER_FAILED_DB;
import static net.furizon.backend.infrastructure.rooms.RoomEmailTexts.TEMPLATE_EXCHANGE_ROOM_FAILED_DB;

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


            log.debug("[ROOM_EXCHANGE] Loaded params: sourceOrder=({}) targetOrder=({})",
                sourceOrder.toFullString(), targetOrder.toFullString());

            final String comment =
                      " was created for a room exchange between orders " + sourceOrderCode + " -> " + targetOrderCode
                    + " happened on " + PRETIX_DATETIME_FORMAT.format(LocalDate.now());
            final String paymentComment = "Payment" + comment + ". DO NOT REFUND ANY PAYMENT FROM THIS ORDER!";
            final String refundComment = "Refund" + comment;

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
            defaultRoomLogic.logExchangeError(pretixRes, 104, targetUsrId, sourceUsrId, event);

            //If successful, exchange rooms in db
            boolean dbRes = false;
            if (pretixRes) {
                dbRes = defaultRoomLogic.exchangeRoom(
                    targetUsrId, sourceUsrId, targetRoomId, sourceRoomId, event, pretixInformation);
                defaultRoomLogic.logExchangeError(dbRes, 105, targetUsrId, sourceUsrId, event);

                if (!dbRes) {
                    //Alert that room swap was successful in pretix, but not in our database
                    emailSender.prepareAndSendForPermission(
                        Permission.PRETIX_ADMIN,
                        TranslatableValue.ofEmail("mail.exchange_room_failed_db.title"),
                        TEMPLATE_EXCHANGE_ROOM_FAILED_DB,
                        MailVarPair.of(FURSONA_NAME, String.valueOf(sourceUsrId)),
                        MailVarPair.of(OTHER_FURSONA_NAME, String.valueOf(targetUsrId)),
                        MailVarPair.of(ORDER_CODE, sourceOrderCode),
                        MailVarPair.of(OTHER_ORDER_CODE, targetOrderCode)
                    );
                }
            }

            //Refresh order
            boolean res = dbRes;
            if (dbRes) {
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
                res = fetchAndUpdateOrder.apply(targetOrderCode);
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
            defaultRoomLogic.logExchangeError(pretixRes, 201, targetUsrId, sourceUsrId, event);

            //Changes in DB
            boolean dbRes = false;
            if (pretixRes) {
                dbRes = defaultRoomLogic.exchangeFullOrder(targetUsrId, sourceUsrId, roomId, event, pretixInformation);
                defaultRoomLogic.logExchangeError(dbRes, 202, targetUsrId, sourceUsrId, event);

                if (!dbRes) {
                    //Alert that room swap was successful in pretix, but not in our database
                    emailSender.prepareAndSendForPermission(
                        Permission.PRETIX_ADMIN,
                        TranslatableValue.ofEmail("mail.exchange_fullorder_failed_db.title"),
                        TEMPLATE_EXCHANGE_FULLORDER_FAILED_DB,
                        MailVarPair.of(FURSONA_NAME, String.valueOf(sourceUsrId)),
                        MailVarPair.of(OTHER_FURSONA_NAME, String.valueOf(targetUsrId)),
                        MailVarPair.of(ORDER_CODE, orderCode)
                    );
                }
            }

            //Refetch pretix order and update it in db
            boolean res = dbRes;
            if (dbRes) {
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
            boolean originallyHadRoomPosition,
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
                + "originalItemId={} newItemId={} newItemPrice={} originallyHadRoomPosition={} "
                + "positionId={} addonToPositionId={} userId={} newRoomItemId={} orderCode={} "
                + "event={} pretixInformation={} createTempAddonFirst={}",
                originalItemId, newItemId, newItemPrice, originallyHadRoomPosition,
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
            throw new ApiException(translationService.error("room.buy.fail.quota_ended"),
                    RoomErrorCodes.BUY_ROOM_NEW_ROOM_QUOTA_ENDED);
        }

        //If quota is available, try getting the item
        PretixPosition tempPosition = null;
        Long posid = null;
        boolean res;
        if (originallyHadRoomPosition && positionId != null) {
            //Create a temporary position to mantain our quota in case of rollback
            if (originalItemId != null) {
                tempPosition = pushPretixPositionAction.invoke(
                    event, createTempAddonFirst, true, 0L, //price is not important since it's the temp item
                    new PushPretixPositionRequest(
                        orderCode,
                        null,
                        originalItemId
                    ), pretixInformation
                );
                if (tempPosition == null) {
                    log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}:"
                            + "Unable to create temporary position for rollback with item {}",
                            userId, newRoomItemId, event, originalItemId);
                    return null;
                }
            }
            res = updatePretixPositionAction.invoke(event, positionId, true, new UpdatePretixPositionRequest(
                    orderCode,
                    newItemId,
                    newItemPrice
            )) != null;
        } else {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: "
                    + "pushing new position should not be possible",
                    userId, newRoomItemId, event);
            PretixPosition pos = pushPretixPositionAction.invoke(
                event, createTempAddonFirst, true, newItemPrice,
                new PushPretixPositionRequest(
                    orderCode,
                    addonToPositionId,
                    newItemId
                ).setPrice(newItemPrice), pretixInformation
            );
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
            throw new ApiException(translationService.error("room.buy.fail_pretix_sync"),
                    RoomErrorCodes.BUY_ROOM_ERROR_UPDATING_POSITION);
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
        log.debug("[ROOM_BUY] rollbackBuyOrUpgrade called with params: "
            + "rollbackItemId={} rollbackPrice={} positionId={} orderCode={} "
            + "originallyHadAroomPosition={} userId={} newRoomItemId={} event={}",
            rollbackItemId, rollbackPrice, positionId, orderCode,
            originallyHadAroomPosition, userId, newRoomItemId, event);

        if (rollbackItemId == null || rollbackPrice == null) {
            log.error("[ROOM_BUY] User {} buying roomItemId {} on event {}: rollbackItemId was null",
                    userId, newRoomItemId, event);
            return;
        }

        boolean res;
        if (originallyHadAroomPosition) {
            res = updatePretixPositionAction.invoke(event, positionId, true, new UpdatePretixPositionRequest(
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
    public boolean buyOrUpgradeRoom(
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
