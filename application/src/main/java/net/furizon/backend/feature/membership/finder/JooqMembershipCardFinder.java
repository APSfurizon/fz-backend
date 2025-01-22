package net.furizon.backend.feature.membership.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.membership.dto.FullInfoMembershipCard;
import net.furizon.backend.feature.membership.dto.MembershipCard;
import net.furizon.backend.feature.membership.mapper.FullInfoMembershipMapper;
import net.furizon.backend.feature.membership.mapper.MembershipCardMapper;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.user.dto.UserDisplayDataWithOrderCode;
import net.furizon.backend.feature.user.mapper.JooqUserDisplayMapper;
import net.furizon.backend.infrastructure.membership.MembershipYearUtils;
import net.furizon.backend.infrastructure.pretix.model.OrderStatus;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record6;
import org.jooq.SelectJoinStep;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.furizon.jooq.generated.Tables.AUTHENTICATIONS;
import static net.furizon.jooq.generated.Tables.MEDIA;
import static net.furizon.jooq.generated.Tables.MEMBERSHIP_CARDS;
import static net.furizon.jooq.generated.Tables.ORDERS;
import static net.furizon.jooq.generated.Tables.USERS;
import static net.furizon.jooq.generated.tables.MembershipInfo.MEMBERSHIP_INFO;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqMembershipCardFinder implements MembershipCardFinder {
    @NotNull private final MembershipYearUtils membershipYearUtils;
    @NotNull private final SqlQuery sqlQuery;

    @Override
    public int countCardsPerUserPerEvent(long userId, @NotNull Event event) {
        short year = event.getMembershipYear(membershipYearUtils);

        return sqlQuery.count(
                PostgresDSL
                        .select(MEMBERSHIP_CARDS.CARD_DB_ID)
                        .from(MEMBERSHIP_CARDS)
                        .where(
                                MEMBERSHIP_CARDS.USER_ID.eq(userId)
                                    .and(MEMBERSHIP_CARDS.ISSUE_YEAR.eq(year))
                        )
        );
    }

    @Nullable
    @Override
    public MembershipCard getMembershipCardByOrderId(long orderId) {
        return sqlQuery.fetchFirst(
            membershipSelect()
            .where(MEMBERSHIP_CARDS.CREATED_FOR_ORDER.eq(orderId))
        ).mapOrNull(MembershipCardMapper::map);
    }

    @Override
    public @Nullable MembershipCard getMembershipCardByCardId(long cardId) {
        return sqlQuery.fetchFirst(
            membershipSelect()
            .where(MEMBERSHIP_CARDS.CARD_DB_ID.eq(cardId))
        ).mapOrNull(MembershipCardMapper::map);
    }

    private @NotNull SelectJoinStep<Record6<Long, Long, Integer, Short, Boolean, Long>> membershipSelect() {
        return PostgresDSL
                .select(
                        MEMBERSHIP_CARDS.USER_ID,
                        MEMBERSHIP_CARDS.CARD_DB_ID,
                        MEMBERSHIP_CARDS.ID_IN_YEAR,
                        MEMBERSHIP_CARDS.ISSUE_YEAR,
                        MEMBERSHIP_CARDS.ALREADY_REGISTERED,
                        MEMBERSHIP_CARDS.CREATED_FOR_ORDER
                )
                .from(MEMBERSHIP_CARDS);
    }

    @NotNull
    @Override
    public List<FullInfoMembershipCard> getMembershipCards(short year) {
        return getMembershipCards(year, 0);
    }

    @Override
    public @NotNull List<FullInfoMembershipCard> getMembershipCards(short year, int afterCardNo) {
        return sqlQuery.fetch(
            PostgresDSL.select(
                MEMBERSHIP_CARDS.USER_ID,
                MEMBERSHIP_CARDS.CARD_DB_ID,
                MEMBERSHIP_CARDS.ID_IN_YEAR,
                MEMBERSHIP_CARDS.ISSUE_YEAR,
                MEMBERSHIP_CARDS.ALREADY_REGISTERED,
                MEMBERSHIP_CARDS.CREATED_FOR_ORDER,
                MEMBERSHIP_INFO.ID,
                MEMBERSHIP_INFO.INFO_FIRST_NAME,
                MEMBERSHIP_INFO.INFO_LAST_NAME,
                MEMBERSHIP_INFO.INFO_FISCAL_CODE,
                MEMBERSHIP_INFO.INFO_BIRTH_CITY,
                MEMBERSHIP_INFO.INFO_BIRTH_REGION,
                MEMBERSHIP_INFO.INFO_BIRTH_COUNTRY,
                MEMBERSHIP_INFO.INFO_BIRTHDAY,
                MEMBERSHIP_INFO.INFO_ADDRESS,
                MEMBERSHIP_INFO.INFO_ZIP,
                MEMBERSHIP_INFO.INFO_CITY,
                MEMBERSHIP_INFO.INFO_REGION,
                MEMBERSHIP_INFO.INFO_COUNTRY,
                MEMBERSHIP_INFO.INFO_PHONE_PREFIX,
                MEMBERSHIP_INFO.INFO_PHONE,
                MEMBERSHIP_INFO.LAST_UPDATED_EVENT_ID,
                MEMBERSHIP_INFO.INFO_ALLERGIES,
                MEMBERSHIP_INFO.USER_ID,
                AUTHENTICATIONS.AUTHENTICATION_EMAIL,
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                USERS.USER_LOCALE,
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_ID,
                MEDIA.MEDIA_TYPE,
                ORDERS.ORDER_SPONSORSHIP_TYPE,
                ORDERS.ORDER_CODE
            )
            .from(MEMBERSHIP_CARDS)
            .innerJoin(USERS)
            .on(
                MEMBERSHIP_CARDS.USER_ID.eq(USERS.USER_ID)
                .and(MEMBERSHIP_CARDS.ISSUE_YEAR.eq(year))
                .and(MEMBERSHIP_CARDS.ID_IN_YEAR.greaterThan(afterCardNo))
            )
            .innerJoin(MEMBERSHIP_INFO)
            .on(USERS.USER_ID.eq(MEMBERSHIP_INFO.USER_ID))
            .innerJoin(AUTHENTICATIONS)
            .on(USERS.USER_ID.eq(AUTHENTICATIONS.USER_ID))
            .leftJoin(MEDIA)
            .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
            .leftJoin(ORDERS)
            .on(ORDERS.ID.eq(MEMBERSHIP_CARDS.CREATED_FOR_ORDER))
            .orderBy(MEMBERSHIP_CARDS.ID_IN_YEAR)
        ).stream().map(r -> r.map(FullInfoMembershipMapper::map)).toList();
    }

    @Override
    public @NotNull List<UserDisplayDataWithOrderCode> getUsersAtEventWithoutMembershipCard(@NotNull Event event) {
        return sqlQuery.fetch(
            PostgresDSL.select(
                USERS.USER_ID,
                USERS.USER_FURSONA_NAME,
                USERS.USER_LOCALE,
                MEDIA.MEDIA_PATH,
                MEDIA.MEDIA_ID,
                MEDIA.MEDIA_TYPE,
                ORDERS.ORDER_SPONSORSHIP_TYPE,
                ORDERS.ORDER_CODE
            )
            .from(USERS)
            .innerJoin(ORDERS)
            .on(
                ORDERS.USER_ID.eq(USERS.USER_ID)
                .and(ORDERS.EVENT_ID.eq(event.getId()))
                .and(ORDERS.ORDER_STATUS.eq((short) OrderStatus.PAID.ordinal()))
                .and(ORDERS.USER_ID.notIn(
                    PostgresDSL.select(MEMBERSHIP_CARDS.USER_ID)
                    .from(MEMBERSHIP_CARDS)
                    .where(MEMBERSHIP_CARDS.ISSUE_YEAR.eq(event.getMembershipYear(membershipYearUtils)))
                ))
            )
            .leftJoin(MEDIA)
            .on(USERS.MEDIA_ID_PROPIC.eq(MEDIA.MEDIA_ID))
        ).stream().map(JooqUserDisplayMapper::mapWithOrderCode).toList();
    }

    @Override
    public boolean canDeleteCard(@NotNull MembershipCard card) {
        return !sqlQuery.fetchFirst(
                PostgresDSL.select(MEMBERSHIP_CARDS.CARD_DB_ID)
                .from(MEMBERSHIP_CARDS)
                .where(
                    MEMBERSHIP_CARDS.ISSUE_YEAR.eq(card.getIssueYear())
                    .and(MEMBERSHIP_CARDS.ID_IN_YEAR.greaterOrEqual(card.getIdInYear()))
                    .and(MEMBERSHIP_CARDS.ALREADY_REGISTERED.isTrue())
                )
                .limit(1)
        ).isPresent();
    }
}
