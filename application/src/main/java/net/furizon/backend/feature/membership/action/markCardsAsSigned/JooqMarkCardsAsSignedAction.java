package net.furizon.backend.feature.membership.action.markCardsAsSigned;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collection;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;

@Component
@RequiredArgsConstructor
public class JooqMarkCardsAsSignedAction implements MarkCardsAsSignedAction {
    @NotNull
    private final SqlCommand sqlCommand;

    @Override
    public boolean invoke(Collection<Long> membershipCardIds) {
        return sqlCommand.execute(
            PostgresDSL.update(MEMBERSHIP_CARDS)
            .set(MEMBERSHIP_CARDS.SIGNED_AT, OffsetDateTime.now())
            .where(MEMBERSHIP_CARDS.CARD_DB_ID.in(membershipCardIds))
        ) == membershipCardIds.size();
    }
}
