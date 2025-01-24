package net.furizon.backend.feature.membership.mapper;

import jakarta.validation.constraints.NotNull;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;

public class MembershipCardMapper {
    @NotNull
    public static MembershipCard map(Record record) {
        return new MembershipCard(
                record.get(MEMBERSHIP_CARDS.CARD_DB_ID),
                record.get(MEMBERSHIP_CARDS.ID_IN_YEAR),
                record.get(MEMBERSHIP_CARDS.ISSUE_YEAR),
                record.get(MEMBERSHIP_CARDS.USER_ID),
                record.get(MEMBERSHIP_CARDS.CREATED_FOR_ORDER),
                record.get(MEMBERSHIP_CARDS.ALREADY_REGISTERED)
        );
    }

    @NotNull
    public static MembershipCard mapOrNull(Record record) {
        Long cardId = record.get(MEMBERSHIP_CARDS.CARD_DB_ID);
        Integer idInYear = record.get(MEMBERSHIP_CARDS.ID_IN_YEAR);
        Short issueYear = record.get(MEMBERSHIP_CARDS.ISSUE_YEAR);
        Long userId = record.get(MEMBERSHIP_CARDS.USER_ID);
        Boolean alreadyRegistered = record.get(MEMBERSHIP_CARDS.ALREADY_REGISTERED);

        if (cardId == null || idInYear == null || issueYear == null || userId == null || alreadyRegistered == null) {
            return null;
        }

        return new MembershipCard(
                cardId,
                idInYear,
                issueYear,
                userId,
                record.get(MEMBERSHIP_CARDS.CREATED_FOR_ORDER),
                alreadyRegistered
        );
    }
}
