package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.order.action.pushAnswer.PushPretixAnswerAction;
import net.furizon.backend.feature.pretix.objects.order.action.updateOrder.UpdateOrderAction;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.pretix.PretixOrderFinder;
import net.furizon.backend.feature.pretix.objects.order.usecase.UpdateOrderInDb;
import net.furizon.backend.feature.pretix.ordersworkflow.OrderWorkflowErrorCode;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.configuration.FrontendConfig;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.EmailVars;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.pretix.PretixConst;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import net.furizon.backend.infrastructure.web.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static net.furizon.backend.infrastructure.pretix.PretixEmailTexts.TEMPLATE_DUPLICATE_ORDER;

@Data
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterUserOrder implements UseCase<RegisterUserOrder.Input, Boolean> {
    @NotNull private final PushPretixAnswerAction pushPretixAnswerAction;
    @NotNull private final PretixOrderFinder pretixOrderFinder;
    @NotNull private final UpdateOrderAction updateOrderAction;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final UserFinder userFinder;
    @NotNull private final EmailSender mailService;
    @NotNull private final UpdateOrderInDb updateOrderInDb;
    @NotNull private final TranslationService translationService;
    @NotNull private final FrontendConfig frontendConfig;

    @Override
    public @NotNull Boolean executor(@NotNull RegisterUserOrder.Input input) {
        try {
            OrderController.suspendWebhook();
            final PretixInformation pretixService = input.pretixService;
            final long userId = input.userId;
            final Event event = pretixService.getCurrentEvent();
            log.info("[PRETIX] User {} is trying to claim order {} with secret {} (checkSecret = {})",
                    userId, input.code, input.secret, input.checkSecret);
            if (input.checkSecret) {
                Objects.requireNonNull(input.secret);
            }

            Order order = orderFinder.findOrderByCodeEvent(input.code, event.getId(), pretixService);
            if (order == null) {
                //If order is not in the db, try to fetch it
                var eventInfo = event.getOrganizerAndEventPair();
                Optional<PretixOrder> pretixOrder = pretixOrderFinder.fetchOrderByCode(
                        eventInfo.getOrganizer(),
                        eventInfo.getEvent(),
                        input.code
                );
                if (!pretixOrder.isPresent()) {
                    log.error("[PRETIX] Registration of order {} failed: "
                            + "Order was not in the db and we were unable to fetch it from pretix", input.code);
                    throw new ApiException(translationService.error("pretix.orders_flow.order_fetch_fail"),
                        OrderWorkflowErrorCode.ORDER_NOT_FOUND);
                }

                var o = updateOrderInDb.execute(pretixOrder.get(), event, pretixService);
                if (!o.isPresent()) {
                    log.error("[PRETIX] Registration of order {} failed: "
                            + "Order was not in the db and an error occurred in "
                            + "parsing or storing the newly fetched order", input.code);
                    throw new ApiException(translationService.error("pretix.orders_flow.order_parse_fail"),
                        OrderWorkflowErrorCode.ORDER_NOT_FOUND);
                }
                order = o.get();
            }

            //Check if secret matches
            if (input.checkSecret && !order.getPretixOrderSecret().equals(input.secret)) {
                throw new ApiException(translationService.error("pretix.orders_flow.order_secret_check_fail"),
                    OrderWorkflowErrorCode.ORDER_SECRET_NOT_MATCH);
            }

            //Check if user already owns an order
            Order prevOrder = orderFinder.findOrderByUserIdEvent(userId, event, pretixService);
            if (prevOrder != null) {
                if (input.immediateErrorOnDuplicateOrder) {
                    throw new ApiException(translationService.error("pretix.orders_flow.order_multiple"),
                        OrderWorkflowErrorCode.ORDER_MULTIPLE_DONE);
                }
                //If the order the user owns is the same we're trying to claim, do nothing and return
                if (prevOrder.getCode().equals(input.code)) {
                    log.debug("[PRETIX] User {} already owned order {}", userId, input.code);
                    return true;
                }
                //We detected a duplicate order for a user.
                log.error("[PRETIX] Registration of order {} failed: User already owns an order!", input.code);
                //We update the duplicate "frontend" field for that order, so pretix admins can verify that this order
                // was really tried to be claimed by the current user
                String prevOrderCode = prevOrder.getCode();
                UserEmailData mail = userFinder.getMailDataForUser(userId);
                if (mail != null) {
                    var v = order.getAnswer(PretixConst.QUESTIONS_DUPLICATE_DATA);
                    String prevDuplicateData = v.map(o -> (String) o).orElse("");
                    prevDuplicateData +=
                            (prevDuplicateData.isEmpty() ? "" : "\n----------------------------\n")
                            + "UserId: " + userId
                            + "\nFursona name: " + (mail == null ? "-" : mail.getFursonaName())
                            + "\nUser email: " + (mail == null ? "-" : mail.getEmail())
                            + "\nOriginal order code: " + prevOrderCode;
                    order.setAnswer(PretixConst.QUESTIONS_DUPLICATE_DATA, prevDuplicateData);
                    boolean success = pushPretixAnswerAction.invoke(order, pretixService);
                    if (!success) {
                        log.warn("[PRETIX] Unable to update duplicate order data for order {} of user {}",
                                input.code, userId);
                    }

                    //We send an email to alert the user
                    var eventNames = event.getEventNames();
                    mailService.fireAndForget(
                        new MailRequest(
                            mail, TEMPLATE_DUPLICATE_ORDER,
                            MailVarPair.of(EmailVars.ORDER_CODE, prevOrderCode),
                            MailVarPair.of(EmailVars.DUPLICATE_ORDER_CODE, order.getCode()),
                            MailVarPair.of(EmailVars.LINK, frontendConfig.getReservationPageUrl()),
                            MailVarPair.of(EmailVars.EVENT_NAME, eventNames == null
                                                                 ? ""
                                                                 : event.getLocalizedName(translationService))
                        ).subject("mail.order_duplicate_detected.title")
                    );
                } else {
                    log.error("[PRETIX] Unable to send duplicate order mail to user {} with order {} ",
                        userId, input.code);
                }

                throw new ApiException(translationService.error("pretix.orders_flow.order_multiple"),
                    OrderWorkflowErrorCode.ORDER_MULTIPLE_DONE);
            }

            //If this order is already owned by someone who, for some reasons,
            // owns more than one order we can reclaim it
            Long prevOwnerId = order.getOrderOwnerUserId();
            if (prevOwnerId != null && prevOwnerId > 0L) {
                int ordersNoPrevOwner = orderFinder.countOrdersOfUserOnEvent(prevOwnerId, event);
                if (ordersNoPrevOwner <= 1) {
                    log.error("[PRETIX] Registration of order {} failed: The order was already owned by {}",
                            input.code, prevOwnerId);
                    throw new ApiException(translationService.error("pretix.orders_flow.order_already_claimed"),
                            OrderWorkflowErrorCode.ORDER_ALREADY_OWNED_BY_SOMEBODY_ELSE);
                }
                log.info("[PRETIX] Order {} was already owned by {} (who had multiple orders)."
                        + "Changing ownership to {}",
                        input.code, prevOwnerId, userId);
            }

            order.setOrderOwnerUserId(userId);
            boolean success = pushPretixAnswerAction.invoke(order, pretixService);

            if (!success) {
                log.error("[PRETIX] Registration of order {} failed:"
                        + "An error occurred while pushing answers to pretix!", input.code);
                throw new ApiException(translationService.error("pretix.answer_push_fail"),
                        OrderWorkflowErrorCode.SERVER_ERROR);
            }

            updateOrderAction.invoke(order, pretixService);
            log.info("[PRETIX] Order {} was successfully claimed by {}!", input.code, userId);

            return true;
        } finally {
            OrderController.resumeWebhook();
        }
    }

    public record Input(
            long userId,
            boolean checkSecret,
            boolean immediateErrorOnDuplicateOrder,
            @NotNull String code,
            @Nullable String secret,
            @NotNull PretixInformation pretixService
    ) {}
}
