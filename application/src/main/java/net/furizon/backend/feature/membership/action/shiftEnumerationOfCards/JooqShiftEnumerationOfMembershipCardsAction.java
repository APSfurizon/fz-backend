package net.furizon.backend.feature.membership.action.shiftEnumerationOfCards;

import lombok.RequiredArgsConstructor;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;

@Component
@RequiredArgsConstructor
public class JooqShiftEnumerationOfMembershipCardsAction implements ShiftEnumerationOfMembershipCardsAction {
    @NotNull private final SqlCommand sqlCommand;
    @NotNull private final SqlQuery sqlQuery;

    @Override
    @Transactional
    public int invoke(short year, int startingFromIdInYear, int shiftAmount) {
        int res = sqlCommand.execute(
            PostgresDSL.update(MEMBERSHIP_CARDS)
            .set(MEMBERSHIP_CARDS.ID_IN_YEAR, MEMBERSHIP_CARDS.ID_IN_YEAR.minus(shiftAmount))
            .where(
                MEMBERSHIP_CARDS.ISSUE_YEAR.eq(year)
                .and(MEMBERSHIP_CARDS.ID_IN_YEAR.greaterOrEqual(startingFromIdInYear))
            )
        );

        var match = sqlQuery.fetchFirst(
            PostgresDSL.select(MEMBERSHIP_CARDS.CARD_DB_ID)
            .from(MEMBERSHIP_CARDS)
            .where(
                MEMBERSHIP_CARDS.ISSUE_YEAR.eq(year)
                .and(MEMBERSHIP_CARDS.ID_IN_YEAR.greaterOrEqual(startingFromIdInYear))
                .and(MEMBERSHIP_CARDS.ALREADY_REGISTERED.isTrue())
            )
            .limit(1)
        );
        if (match.isPresent()) {
            throw new RuntimeException("Tried shifting cards, but one of them was already registered");
        }

        return res;
    }
}
