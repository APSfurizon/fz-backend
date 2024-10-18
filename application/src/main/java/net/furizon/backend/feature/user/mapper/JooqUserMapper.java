package net.furizon.backend.feature.user.mapper;

import net.furizon.backend.feature.user.User;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.USERS;

public class JooqUserMapper {
    @NotNull
    public static User map(Record record) {
        return User.builder()
            .id(record.get(USERS.USER_ID))
            .build();
    }
}
