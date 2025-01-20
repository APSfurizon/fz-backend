package net.furizon.backend.feature.pretix.objects.order.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.action.createMembershipCard.CreateMembershipCardAction;
import net.furizon.backend.feature.membership.action.deleteMembershipCard.DeleteMembershipCardAction;
import net.furizon.backend.feature.membership.action.shiftEnumerationOfCards.ShiftEnumerationOfMembershipCardsAction;
import net.furizon.backend.feature.membership.action.updateMembershipOwner.UpdateMembershipCardOwner;
import net.furizon.backend.feature.membership.dto.FullInfoMembershipCard;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.order.action.convertTicketOnlyOrder.ConvertTicketOnlyOrderAction;
import net.furizon.backend.feature.pretix.objects.order.action.deleteOrder.DeleteOrderAction;
import net.furizon.backend.feature.pretix.objects.order.action.upsertOrder.UpsertOrderAction;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.membership.MembershipYearUtils;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.SUBJECT_MEMBERSHIP_FATAL_ERROR;
import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.SUBJECT_MEMBERSHIP_WARNING;
import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.TEMPLATE_MEMBERSHIP_CARD_ALREADY_REGISTERED;
import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.TEMPLATE_MEMBERSHIP_CARD_DELETED_BUT_REGISTERED;
import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.TEMPLATE_MEMBERSHIP_CARD_OWNER_CHANGED_BUT_REGISTERED;
import static net.furizon.backend.infrastructure.email.EmailVars.MEMBERSHIP_CARD_ID;
import static net.furizon.backend.infrastructure.email.EmailVars.MEMBERSHIP_CARD_ID_IN_YEAR;
import static net.furizon.backend.infrastructure.email.EmailVars.ORDER_CODE;
import static net.furizon.backend.infrastructure.email.EmailVars.ORDER_OWNER_ID;
import static net.furizon.backend.infrastructure.email.EmailVars.ORDER_PREV_OWNER_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateOrderInDb {
    @NotNull private final UpsertOrderAction upsertOrderAction;
    @NotNull private final DeleteOrderAction deleteOrderAction;
    @NotNull private final DeleteMembershipCardAction deleteMembershipCardAction;
    @NotNull private final ShiftEnumerationOfMembershipCardsAction shiftEnumerationAction;
    @NotNull private final CreateMembershipCardAction createMembershipCardAction;
    @NotNull private final UpdateMembershipCardOwner updateMembershipCardOwner;
    @NotNull private final ConvertTicketOnlyOrderAction convertTicketOnlyOrderAction;
    @NotNull private final MembershipCardFinder membershipCardFinder;
    @NotNull private final EmailSender emailSender;
    @NotNull private final UserFinder userFinder;
    @NotNull private final MembershipYearUtils membershipYearUtils;

    @Transactional
    public @NotNull Optional<Order> execute(@NotNull PretixOrder pretixOrder,
                                            @NotNull Event event,
                                            @NotNull PretixInformation pretixInformation) {
        return execute(pretixOrder, event, pretixInformation, true);
    }
    @Transactional
    public @NotNull Optional<Order> execute(@NotNull PretixOrder pretixOrder,
                                            @NotNull Event event,
                                            @NotNull PretixInformation pretixInformation,
                                            boolean updateToRoomWithTicket) {
        try {
            OrderController.suspendWebhook();
            Order order = null;
            boolean shouldDelete = true;

            var orderOpt = pretixInformation.parseOrder(pretixOrder, event);
            if (orderOpt.isPresent()) {
                order = orderOpt.get();
                OrderStatus os = order.getOrderStatus();
                if (os == OrderStatus.PENDING || os == OrderStatus.PAID) {
                    log.debug("[PRETIX] Storing / Updating order: {}@{}", pretixOrder.getCode(), event.getSlug());
                    upsertOrderAction.invoke(order, pretixInformation);

                    // GENERATE MEMBERSHIP CARD LOGIC
                    Long orderOwnerId = order.getOrderOwnerUserId();
                    if (orderOwnerId != null && os == OrderStatus.PAID) {

                        MembershipCard card = membershipCardFinder.getMembershipCardByOrderId(order.getId());
                        if (order.hasMembership()) {

                            if (card == null) { // No membership card has been generated by this order
                                if (userFinder.findById(orderOwnerId) != null) {
                                    log.info("[PRETIX] Generating new membership card for user {}", orderOwnerId);
                                    createMembershipCardAction.invoke(orderOwnerId, event, order);
                                } else {
                                    log.warn("[PRETIX] Tried generating card for user {}, "
                                                    + "but it doesn't exist in the DB",
                                            orderOwnerId);
                                }

                            } else { //A membership card was already created with this order
                                long cardOwnerId = card.getUserOwnerId();

                                //Order owner has changed
                                if (!orderOwnerId.equals(cardOwnerId)) {
                                    //If the card wasn't registered yet, we can change the owners
                                    if (!card.isRegistered()) {
                                        if (userFinder.findById(orderOwnerId) != null) {
                                            log.warn("[PRETIX] Order {} has already generated "
                                                            + "the membeship card {}/{}. "
                                                            + "However, the order owner "
                                                            + "has changed ({} -> {}) and, since the card "
                                                            + "was not registered yet, "
                                                            + "the card's owner is now changed as well",
                                                    order.getCode(),
                                                    card.getCardId(), card.getIdInYear(),
                                                    cardOwnerId, orderOwnerId
                                            );
                                            updateMembershipCardOwner.invoke(card, orderOwnerId);
                                        } else {
                                            log.warn("[PRETIX] Tried changing card owner {} -> {}, "
                                                            + "but the new one doesn't exist in the DB",
                                                    cardOwnerId, orderOwnerId);
                                        }

                                    } else { //Otherwise, send an error email to web admins
                                        log.error("[PRETIX] Order {} has already generated the membeship card {}/{}. "
                                                        + "Order owner has changed ({} -> {}), "
                                                        + "but membership card was already registered!!"
                                                        + "Manual fix is needed!!! +++",
                                                order.getCode(),
                                                card.getCardId(), card.getIdInYear(),
                                                cardOwnerId, orderOwnerId
                                        );

                                        String idInYear = String.valueOf(card.getIdInYear());
                                        emailSender.sendToPermission(
                                            Permission.CAN_MANAGE_MEMBERSHIP_CARDS,
                                            SUBJECT_MEMBERSHIP_FATAL_ERROR,
                                                TEMPLATE_MEMBERSHIP_CARD_OWNER_CHANGED_BUT_REGISTERED,
                                            MailVarPair.of(ORDER_CODE, order.getCode()),
                                            MailVarPair.of(MEMBERSHIP_CARD_ID, String.valueOf(card.getCardId())),
                                            MailVarPair.of(MEMBERSHIP_CARD_ID_IN_YEAR, idInYear),
                                            MailVarPair.of(ORDER_PREV_OWNER_ID, String.valueOf(cardOwnerId)),
                                            MailVarPair.of(ORDER_OWNER_ID, String.valueOf(orderOwnerId))
                                        );
                                    }
                                }
                            }
                        } else {
                            if (card != null) { //The order HAD a membership card, but now it does not anymore
                                if (!card.isRegistered()) {
                                    short year = event.getMembershipYear(membershipYearUtils);
                                    int cardIdInYear = card.getIdInYear();
                                    List<FullInfoMembershipCard> cards = membershipCardFinder.getMembershipCards(
                                            year, cardIdInYear
                                    );
                                    boolean canDelete = cards.stream().noneMatch(
                                            c -> c.getMembershipCard().isRegistered()
                                    );

                                    if (canDelete) {
                                        log.info("[PRETIX] Order {} previously had a membership card, "
                                                + "but now it doesn't. "
                                                + "Deleting the previous registered card {}/{} and "
                                                + "shifting the enumeration of the others",
                                                order.getCode(), card.getCardId(), cardIdInYear);
                                        boolean del = deleteMembershipCardAction.invoke(card);
                                        if (del) {
                                            int res = shiftEnumerationAction.invoke(year, cardIdInYear, 1);
                                            log.info("[PRETIX] Shifted {} cards after deleting card {}/{}",
                                                    res, card.getCardId(), cardIdInYear);
                                        } else {
                                            log.error("Failed deleting card {}/{} after order change",
                                                    card.getCardId(), cardIdInYear);
                                        }
                                    } else {
                                        emailSender.sendToPermission(
                                            Permission.CAN_MANAGE_MEMBERSHIP_CARDS,
                                            SUBJECT_MEMBERSHIP_FATAL_ERROR,
                                            TEMPLATE_MEMBERSHIP_CARD_DELETED_BUT_REGISTERED,
                                            MailVarPair.of(ORDER_CODE, order.getCode()),
                                            MailVarPair.of(MEMBERSHIP_CARD_ID, String.valueOf(card.getCardId())),
                                            MailVarPair.of(MEMBERSHIP_CARD_ID_IN_YEAR, String.valueOf(cardIdInYear))
                                        );
                                    }
                                } else {
                                    log.error("[PRETIX] Order {} previously had a membership card, but now it doesn't. "
                                                    + "However, the previous membership card ({}/{}) "
                                                    + "was already registered!! "
                                                    + "No operation is going to be performed.",
                                            order.getCode(), card.getCardId(), card.getIdInYear());

                                    emailSender.sendToPermission(
                                        Permission.CAN_MANAGE_MEMBERSHIP_CARDS,
                                        SUBJECT_MEMBERSHIP_WARNING,
                                            TEMPLATE_MEMBERSHIP_CARD_ALREADY_REGISTERED,
                                        MailVarPair.of(ORDER_CODE, order.getCode()),
                                        MailVarPair.of(MEMBERSHIP_CARD_ID, String.valueOf(card.getCardId())),
                                        MailVarPair.of(MEMBERSHIP_CARD_ID_IN_YEAR, String.valueOf(card.getIdInYear()))
                                    );
                                }
                            }
                        }
                    }

                    shouldDelete = false;
                } else {
                    order = null;
                }
            } else {
                log.error("[PRETIX] Unable to parse order: {}@{}", pretixOrder.getCode(), event.getSlug());
            }

            if (shouldDelete) {
                deleteOrderAction.invoke(pretixOrder.getCode());
            }

            if (updateToRoomWithTicket && order != null && order.getRoomPositionId() == null && !order.isDaily()) {
                //If we convert a pending order, all pending payments will be canceled
                if (order.getOrderStatus() != OrderStatus.PENDING) {
                    log.info("[PRETIX] Order {} is a 'ticket only' order. Converting it to ticket + room",
                        order.getCode());
                    convertTicketOnlyOrderAction.invoke(order, pretixInformation, this);
                } else {
                    log.warn("[PRETIX] Order {} is a 'ticket only' order, but we're skipping convertion "
                            + "because of its status: {}", order.getCode(), order.getOrderStatus());
                }
            }

            return Optional.ofNullable(order);
        } finally {
            OrderController.resumeWebhook();
        }
    }
}
