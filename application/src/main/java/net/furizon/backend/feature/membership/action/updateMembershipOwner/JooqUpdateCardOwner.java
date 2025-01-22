package net.furizon.backend.feature.membership.action.updateMembershipOwner;

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
public class JooqUpdateCardOwner implements UpdateMembershipCardOwner {
    @NotNull
    private final SqlCommand sqlCommand;

    @Override
    public void invoke(@NotNull MembershipCard card, long newOwnerId) {
        log.info("Updating membership card {} owner to {}", card, newOwnerId);
        sqlCommand.execute(
            PostgresDSL.update(MEMBERSHIP_CARDS)
            .set(MEMBERSHIP_CARDS.USER_ID, newOwnerId)
            .where(MEMBERSHIP_CARDS.CARD_DB_ID.eq(card.getCardId()))
        );
    }
}
