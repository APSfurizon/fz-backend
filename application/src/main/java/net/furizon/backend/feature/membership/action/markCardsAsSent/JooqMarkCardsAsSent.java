package net.furizon.backend.feature.membership.action.markCardsAsSent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;

@Slf4j
@Component
@RequiredArgsConstructor
class JooqMarkCardsAsSent implements MarkCardsAsSentAction {
    @NotNull
    private final SqlCommand sqlCommand;

    @Override
    public boolean invoke(@NotNull Collection<Long> cardIds) {
        return sqlCommand.execute(
            PostgresDSL.update(MEMBERSHIP_CARDS)
            .set(MEMBERSHIP_CARDS.SENT_BY_EMAIL, PostgresDSL.trueCondition())
            .where(MEMBERSHIP_CARDS.CARD_DB_ID.in(cardIds))
        ) == cardIds.size();
    }
}
