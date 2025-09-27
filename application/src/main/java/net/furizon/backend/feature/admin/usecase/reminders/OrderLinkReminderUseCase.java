package net.furizon.backend.feature.admin.usecase.reminders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.pretix.objects.order.Order;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.infrastructure.admin.ReminderEmailTexts;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.EmailVars;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.pretix.PretixConfig;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderLinkReminderUseCase implements UseCase<PretixInformation, Integer> {
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final EmailSender emailSender;
    @NotNull private final PretixConfig pretixConfig;

    @Override
    public @NotNull Integer executor(@NotNull PretixInformation pretixInformation) {
        int n = 0;
        log.info("Sending order linking remind emails");

        List<Order> unlinkedOrders = orderFinder.getUnlinkedOrder(
                pretixInformation,
                pretixInformation.getCurrentEvent()
        );

        MailRequest[] mails = new MailRequest[unlinkedOrders.size()];
        for (Order o : unlinkedOrders) {
            String buyerEmail = o.getBuyerEmail();
            if (buyerEmail == null || o.getOrderStatus() != OrderStatus.PAID) {
                continue;
            }

            log.info("Sending order linking reminder email to {}", buyerEmail);
            MailRequest mail = new MailRequest();
            Locale orderLocale = o.getBuyerLocale() == null ? Locale.UK : Locale.of(o.getBuyerLocale());
            mail.to(orderLocale, buyerEmail);
            mail.subject(ReminderEmailTexts.SUBJECT_ORDER_LINK);
            mail.templateMessage(ReminderEmailTexts.TEMPLATE_ORDER_LINK, null,
                MailVarPair.of(EmailVars.LINK, pretixConfig.getShop().getOrderUrl(o)),
                MailVarPair.of(EmailVars.ORDER_CODE, o.getCode())
            );

            mails[n] = mail;
            n++;
        }
        //It's fine having some mails set to null
        log.info("Firing order linking emails");
        emailSender.fireAndForgetMany(mails);

        return n;
    }
}
