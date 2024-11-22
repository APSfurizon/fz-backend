package net.furizon.backend.feature.membership.action.setCardRegistrationStatus;


import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;

@Component
@RequiredArgsConstructor
public class JooqSetCardRegistrationStatus implements SetMembershipCardRegistrationStatus {
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public void invoke(long membershipCardId, boolean status) {
        sqlCommand.execute(
                PostgresDSL
                        .update(MEMBERSHIP_CARDS)
                        .set(MEMBERSHIP_CARDS.ALREADY_REGISTERED, status)
                        .where(MEMBERSHIP_CARDS.CARD_DB_ID.eq(membershipCardId))
        );
    }
}
