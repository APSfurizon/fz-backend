package net.furizon.backend.feature.membership.mapper;

import net.furizon.backend.feature.membership.dto.FullInfoMembershipCard;

import net.furizon.backend.feature.user.mapper.JooqDisplayUserMapper;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;

import static net.furizon.jooq.generated.Tables.AUTHENTICATIONS;

public class FullInfoMembershipMapper {
    @NotNull
    public static FullInfoMembershipCard map(Record record) {
        return FullInfoMembershipCard.builder()
            .membershipCard(MembershipCardMapper.map(record))
            .userInfo(MembershipInfoMapper.map(record))
            .user(JooqDisplayUserMapper.map(record))
            .email(record.get(AUTHENTICATIONS.AUTHENTICATION_EMAIL))
            .build();
    }
}
