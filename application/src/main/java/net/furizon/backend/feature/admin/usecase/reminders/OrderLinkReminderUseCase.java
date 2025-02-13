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
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderLinkReminderUseCase implements UseCase<PretixInformation, Integer> {
    @NotNull private final OrderFinder orderFinder;
    @NotNull private final EmailSender emailSender;

    @Override
    public @NotNull Integer executor(@NotNull PretixInformation pretixInformation) {
        int n = 0;

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

            MailRequest mail = new MailRequest();
            mail.to(buyerEmail);
            mail.subject(ReminderEmailTexts.SUBJECT_ORDER_LINK);
            mail.templateMessage(ReminderEmailTexts.TEMPLATE_ORDER_LINK, null,
                MailVarPair.of(EmailVars.LINK, )
            );

            mails[n] = mail;
            n++;
        }
        emailSender.fireAndForgetMany(mails);

        return n;
    }
}
