package net.furizon.backend.feature.membership.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.action.markCardsAsSent.MarkCardsAsSentAction;
import net.furizon.backend.feature.membership.dto.ExportedMembershipCard;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.infrastructure.configuration.MembershipConfig;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.EmailVars;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.membership.MembershipYearUtils;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static net.furizon.backend.feature.authentication.AuthenticationEmailTexts.TEMPLATE_MEMBERSHIP_CARD_EXPORT_EMAIL;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendMembershipCardEmails implements UseCase<Event, Integer> {
    @NotNull
    private final MarkCardsAsSentAction markCardsAsSentAction;
    @NotNull
    private final MembershipCardFinder membershipCardFinder;
    @NotNull
    private final MembershipYearUtils membershipYearUtils;
    @NotNull
    private final MembershipConfig membershipConfig;
    @NotNull
    private final EmailSender emailSender;

    @Override
    @Transactional
    public @NotNull Integer executor(@NotNull Event event) {
        log.info("Sending membership cards to users");
        OffsetDateTime from = event.getCorrectDateFrom();
        if (from != null) {
            OffsetDateTime sendingDate = from.minusDays(membershipConfig.getSendCardEmailFromMinusDay());

            OffsetDateTime now = OffsetDateTime.now();
            if (now.isBefore(sendingDate)) {
                log.debug("Not sending membership card emails yet, sending date is {}, now = {}", sendingDate, now);
                return 0;
            }
        }

        short year = event.getMembershipYear(membershipYearUtils);
        List<ExportedMembershipCard> cards = membershipCardFinder.getCardsNotSentByEmail(year);
        if (cards.isEmpty()) {
            return 0;
        }

        var cardIds = cards.stream().map(ExportedMembershipCard::getCardDbId).toList();
        log.info("Marking cards [{}] as sent", cardIds);
        markCardsAsSentAction.invoke(cardIds);


        String expirationDate = event.getMembershipResetDate(membershipYearUtils)
                                     .minusDays(1L)
                                     .format(DateTimeFormatter.ISO_LOCAL_DATE);

        int i = 0;
        MailRequest[] mails = new MailRequest[cards.size()];
        for (ExportedMembershipCard card : cards) {
            log.info("Sending membership card email to {}", card.getUser().getEmail());
            String cardNo = MembershipCard.toNumber(year, card.getCardIdInYear());
            mails[i++] = new MailRequest(
                    card.getUser(),
                    TEMPLATE_MEMBERSHIP_CARD_EXPORT_EMAIL,
                    MailVarPair.of(EmailVars.MEMBERSHIP_CARD_ID_IN_YEAR, cardNo),
                    MailVarPair.of(EmailVars.LAST_NAME, card.getLastName()),
                    MailVarPair.of(EmailVars.FIRST_NAME, card.getFirstName()),
                    MailVarPair.of(EmailVars.FISCAL_CODE, card.getFiscalCode() == null ? "" : card.getFiscalCode()),
                    MailVarPair.of(EmailVars.DEADLINE, expirationDate),
                    MailVarPair.of(EmailVars.BIRTHDAY, card.getBirthDay().format(DateTimeFormatter.ISO_LOCAL_DATE))
            ).subject("mail.membership_card_export.title", cardNo);
        }
        log.info("Sending {} card emails", i);
        emailSender.fireAndForgetMany(mails);

        return i;
    }
}
