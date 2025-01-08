package net.furizon.backend.feature.pretix.ordersworkflow.usecase;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.PretixOrder;
import net.furizon.backend.feature.pretix.objects.order.action.pushAnswer.PushPretixAnswerAction;
import net.furizon.backend.feature.pretix.objects.order.action.updateOrder.UpdateOrderAction;
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
import net.furizon.backend.infrastructure.pretix.PretixConst;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.security.FurizonUser;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

import static net.furizon.backend.infrastructure.pretix.PretixEmailTexts.LANG_PRETIX;
import static net.furizon.backend.infrastructure.pretix.PretixEmailTexts.SUBJECT_ORDER_PROBLEM;
import static net.furizon.backend.infrastructure.pretix.PretixEmailTexts.TEMPLATE_DUPLICATE_ORDER;

@Data
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterUserOrder implements UseCase<RegisterUserOrder.Input, RedirectView> {
    @NotNull private final PushPretixAnswerAction pushPretixAnswerAction;
    @NotNull private final PretixOrderFinder pretixOrderFinder;
    @NotNull private final UpdateOrderAction updateOrderAction;
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final EmailSender mailService;
    @NotNull private final FrontendConfig config;
    @NotNull private final UpdateOrderInDb updateOrderInDb;
    private final UserFinder userFinder;

    private RedirectView successView;
    @PostConstruct
    private void init() {
        successView = new RedirectView(config.getOrderHomepageUrl());
    }

    @Override
    public @NotNull RedirectView executor(@NotNull RegisterUserOrder.Input input) {
        final PretixInformation pretixService = input.pretixService;
        final FurizonUser user = input.user;
        if (user == null) {
            log.error("[PRETIX] Registration of order {} failed: User is not logged in", input.code);
            return new RedirectView(config.getLoginRedirectUrl(input.request.getRequestURL().toString()));
        }
        final long userId = user.getUserId();
        final Event event = pretixService.getCurrentEvent();
        log.info("[PRETIX] User {} is trying to claim order {} with secret {}",
                user.getUserId(), input.code, input.secret);

        Order order = orderFinder.findOrderByCodeEvent(input.code, event, pretixService);
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
                return new RedirectView(config.getOrderHomepageUrl(OrderWorkflowErrorCode.SERVER_ERROR));
            }

            var o = updateOrderInDb.execute(pretixOrder.get(), event, pretixService);
            if (!o.isPresent()) {
                log.error("[PRETIX] Registration of order {} failed: "
                        + "Order was not in the db and an error occurred in "
                        + "parsing or storing the newly fetched order", input.code);
                return new RedirectView(config.getOrderHomepageUrl(OrderWorkflowErrorCode.SERVER_ERROR));
            }
            order = o.get();
        }

        //Check if secret matches
        if (!order.getPretixOrderSecret().equals(input.secret)) {
            return new RedirectView(config.getOrderHomepageUrl(OrderWorkflowErrorCode.ORDER_SECRET_NOT_MATCH));
        }

        //Check if user already owns an order
        Order prevOrder = orderFinder.findOrderByUserIdEvent(userId, event, pretixService);
        if (prevOrder != null) {
            //If the order the user owns is the same we're trying to claim, do nothing and return
            if (prevOrder.getCode().equals(input.code)) {
                log.debug("[PRETIX] User {} already owned order {}", userId, input.code);
                return successView;
            }
            //We detected a duplicate order for an user.
            log.error("[PRETIX] Registration of order {} failed: User already owns an order!", input.code);
            //We update the duplicate "frontend" field for that order, so pretix admins can verify that this order
            // was really tried to be claimed by the current user
            String prevOrderCode = prevOrder.getCode();
            UserEmailData mail = userFinder.getMailDataForUser(userId);

            String prevDuplicateData = "";
            var v = order.getAnswer(PretixConst.QUESTIONS_DUPLICATE_DATA);
            if (v.isPresent()) {
                prevDuplicateData = (String) v.get();
            }
            prevDuplicateData +=
                      "\n----------------------------\n"
                    + "\nUserId: " + user.getUserId()
                    + "\nFursona name: " + mail == null ? "-" : mail.getFursonaName()
                    + "\nUser email: " + mail == null ? "-" : mail.getEmail()
                    + "\nOriginal order code: " + prevOrderCode;
            order.setAnswer(PretixConst.QUESTIONS_DUPLICATE_DATA, prevDuplicateData);
            boolean success = pushPretixAnswerAction.invoke(order, pretixService);
            if (!success) {
                log.warn("[PRETIX] Unable to update duplicate order data for order {} of user {}",
                        input.code, userId);
            }

            //We send an email to alert the user
            var eventNames = event.getEventNames();
            mailService.send(mail, SUBJECT_ORDER_PROBLEM, TEMPLATE_DUPLICATE_ORDER,
                new MailVarPair(EmailVars.EVENT_NAME, eventNames == null ? "" : eventNames.get(LANG_PRETIX)),
                new MailVarPair(EmailVars.ORDER_CODE, prevOrderCode),
                new MailVarPair(EmailVars.DUPLICATE_ORDER_CODE, order.getCode())
            );

            return new RedirectView(config.getOrderHomepageUrl(OrderWorkflowErrorCode.ORDER_MULTIPLE_DONE));
        }

        //If this order is already owned by someone who, for some reasons, owns more than one order we can reclaim it
        Long prevOwnerId = order.getOrderOwnerUserId();
        if (prevOwnerId != null && prevOwnerId > 0L) {
            int ordersNoPrevOwner = orderFinder.countOrdersOfUserOnEvent(prevOwnerId, event);
            if (ordersNoPrevOwner <= 1) {
                log.error("[PRETIX] Registration of order {} failed: The order was already owned by {}",
                        input.code, prevOwnerId);
                return new RedirectView(config.getOrderHomepageUrl(
                        OrderWorkflowErrorCode.ORDER_ALREADY_OWNED_BY_SOMEBODY_ELSE));
            }
            log.info("[PRETIX] Order {} was already owned by {} (who had multiple orders). Changing ownership to {}",
                    input.code, prevOwnerId, userId);
        }

        order.setOrderOwnerUserId(userId);
        boolean success = pushPretixAnswerAction.invoke(order, pretixService);

        if (!success) {
            log.error("[PRETIX] Registration of order {} failed: An error occurred while pushing answers to pretix!",
                    input.code);
            return new RedirectView(config.getOrderHomepageUrl(OrderWorkflowErrorCode.SERVER_ERROR));
        }

        updateOrderAction.invoke(order, pretixService);
        log.info("[PRETIX] Order {} was successfully claimed by {}!", input.code, userId);

        return successView;
    }

    public record Input(
            @Nullable FurizonUser user,
            @NotNull String code,
            @NotNull String secret,
            @NotNull HttpServletRequest request,
            @NotNull PretixInformation pretixService
    ) {}
}
