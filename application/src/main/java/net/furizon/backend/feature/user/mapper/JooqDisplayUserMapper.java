package net.furizon.backend.feature.user.mapper;

import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.USERS;

public class JooqDisplayUserMapper {
    @NotNull
    public static UserDisplayData map(Record record) {
        Short sponsor = record.get(ORDERS.ORDER_SPONSORSHIP_TYPE);
        return UserDisplayData.builder()
                .userId(record.get(USERS.USER_ID))
                .fursonaName(record.get(USERS.USER_FURSONA_NAME))
                .locale(record.get(USERS.USER_LOCALE))
                .propicUrl(record.get(MEDIA.MEDIA_PATH))
                .sponsorship(sponsor != null ? Sponsorship.get(sponsor) : null)
            .build();
    }
}
