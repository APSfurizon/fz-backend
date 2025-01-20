package net.furizon.backend.feature.membership.action.deleteMembershipCard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.membership.finder.MembershipCardFinder;
import net.furizon.backend.feature.pretix.objects.order.finder.OrderFinder;
import net.furizon.backend.infrastructure.email.EmailSender;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.SUBJECT_MEMBERSHIP_FATAL_ERROR;
import static net.furizon.backend.feature.authentication.AuthenticationMailTexts.TEMPLATE_MEMBERSHIP_CARD_DELETED_BUT_REGISTERED;
import static net.furizon.backend.infrastructure.email.EmailVars.*;
import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqDeleteMembershipCardAction implements DeleteMembershipCardAction {
    @NotNull private final MembershipCardFinder membershipCardFinder;
    @NotNull private final OrderFinder orderFinder;

    @NotNull private final EmailSender emailSender;
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public boolean invoke(@NotNull MembershipCard card) {
        log.info("Deleting membership card {}", card);

        //This check is also done by DB triggers, but we need to do it again to
        // send email to admins
        if (!membershipCardFinder.canDeleteCard(card)) {
            Long orderId = card.getCreatedForOrderId();
            String orderCode = orderId == null ? "-" : orderFinder.getOrderCodeById(orderId);
            orderCode = orderCode == null ? "-" : orderCode;
            emailSender.sendToPermission(
                Permission.CAN_MANAGE_MEMBERSHIP_CARDS,
                SUBJECT_MEMBERSHIP_FATAL_ERROR,
                TEMPLATE_MEMBERSHIP_CARD_DELETED_BUT_REGISTERED,
                MailVarPair.of(ORDER_CODE, orderCode),
                MailVarPair.of(MEMBERSHIP_CARD_ID, String.valueOf(card.getCardId())),
                MailVarPair.of(MEMBERSHIP_CARD_ID_IN_YEAR, String.valueOf(card.getIdInYear()))
            );
            return false;
        }

        //Card deletion will also trigger renumbering of following cards
        return sqlCommand.execute(
            PostgresDSL.deleteFrom(MEMBERSHIP_CARDS)
            .where(
                MEMBERSHIP_CARDS.CARD_DB_ID.eq(card.getCardId())
                .and(MEMBERSHIP_CARDS.ALREADY_REGISTERED.isFalse())
            )
        ) > 0;
    }
}
