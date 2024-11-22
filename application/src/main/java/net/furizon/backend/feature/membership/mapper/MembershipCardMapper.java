package net.furizon.backend.feature.membership.mapper;

import jakarta.validation.constraints.NotNull;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;

public class MembershipCardMapper {
    @NotNull
    public static MembershipCard map(Record record) {
        return MembershipCard.builder()
                .cardId(record.get(MEMBERSHIP_CARDS.CARD_DB_ID))
                .idInYear(record.get(MEMBERSHIP_CARDS.ID_IN_YEAR))
                .userOwnerId(record.get(MEMBERSHIP_CARDS.USER_ID))
                .issueYear(record.get(MEMBERSHIP_CARDS.ISSUE_YEAR))
                .isRegistered(record.get(MEMBERSHIP_CARDS.ALREADY_REGISTERED))
                .createdForOrderId(record.get(MEMBERSHIP_CARDS.CREATED_FOR_ORDER))
            .build();
    }
}
