package net.furizon.backend.feature.membership.mapper;

import net.furizon.backend.feature.membership.dto.ExportedMembershipCard;
import net.furizon.backend.feature.user.mapper.JooqUserEmailDataMapper;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;
import static net.furizon.jooq.generated.Tables.MEMBERSHIP_INFO;

public class ExportedMembershipCardMapper {
    public static @NotNull ExportedMembershipCard map(@NotNull Record r) {
        return ExportedMembershipCard.builder()
                .cardDbId(r.get(MEMBERSHIP_CARDS.CARD_DB_ID))
                .cardIdInYear(r.get(MEMBERSHIP_CARDS.ID_IN_YEAR))

                .firstName(r.get(MEMBERSHIP_INFO.INFO_FIRST_NAME))
                .lastName(r.get(MEMBERSHIP_INFO.INFO_LAST_NAME))
                .fiscalCode(r.get(MEMBERSHIP_INFO.INFO_FISCAL_CODE))
                .birthDay(r.get(MEMBERSHIP_INFO.INFO_BIRTHDAY))

                .user(JooqUserEmailDataMapper.map(r))
            .build();
    }
}
