package net.furizon.backend.feature.membership.action.setCardRegistrationStatus;


import net.furizon.backend.infrastructure.logging.LogCall;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.order.controller.OrderController;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;

@Component
@RequiredArgsConstructor
@LogCall
public class JooqSetCardRegistrationStatus implements SetMembershipCardRegistrationStatus {
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public boolean invoke(long membershipCardId, boolean status) {
        try {
            OrderController.suspendWebhook();
            return sqlCommand.execute(
                PostgresDSL.update(MEMBERSHIP_CARDS)
                .set(MEMBERSHIP_CARDS.ALREADY_REGISTERED, status)
                .where(
                    MEMBERSHIP_CARDS.CARD_DB_ID.eq(membershipCardId)
                    .and(MEMBERSHIP_CARDS.ID_IN_YEAR.isNotNull())
                )
            ) == 1;
        } finally {
            OrderController.resumeWebhook();
        }
    }
}
