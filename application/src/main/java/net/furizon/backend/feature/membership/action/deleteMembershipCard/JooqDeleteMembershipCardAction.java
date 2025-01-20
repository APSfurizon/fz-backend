package net.furizon.backend.feature.membership.action.deleteMembershipCard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqDeleteMembershipCardAction implements DeleteMembershipCardAction {
    @NotNull private final SqlCommand sqlCommand;

    @Override
    public boolean invoke(@NotNull MembershipCard card) {
        log.info("Deleting membership card {}", card);
        return sqlCommand.execute(
            PostgresDSL.deleteFrom(MEMBERSHIP_CARDS)
            .where(
                MEMBERSHIP_CARDS.CARD_DB_ID.eq(card.getCardId())
                .and(MEMBERSHIP_CARDS.ALREADY_REGISTERED.isFalse())
            )
        ) > 0;
    }
}
