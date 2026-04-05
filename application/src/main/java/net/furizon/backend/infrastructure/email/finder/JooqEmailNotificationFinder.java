package net.furizon.backend.infrastructure.email.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.email.NotificationType;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static net.furizon.jooq.generated.Tables.AUTHENTICATIONS;
import static net.furizon.jooq.generated.Tables.ONE_TIME_NOTIFICATIONS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqEmailNotificationFinder implements EmailNotificationFinder {
    @NotNull private final SqlQuery sqlQuery;

    @Override
    public @NotNull Set<Long> findUserIdsByNotification(@NotNull NotificationType notificationType,
                                                        @NotNull String notificationId) {
        return sqlQuery.fetch(
                PostgresDSL.select(ONE_TIME_NOTIFICATIONS.DEST_USER_ID)
                .from(ONE_TIME_NOTIFICATIONS)
                .where(
                        ONE_TIME_NOTIFICATIONS.NOTIFICATION_IDENTIFIER.eq(notificationId)
                        .and(ONE_TIME_NOTIFICATIONS.NOTIFICATION_TYPE.eq(notificationType.getId()))
                )
        ).stream().map(r -> r.get(ONE_TIME_NOTIFICATIONS.DEST_USER_ID)).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public @NotNull Set<String> findUserEmailsByNotification(@NotNull NotificationType notificationType,
                                                             @NotNull String notificationId) {
        return sqlQuery.fetch(
                PostgresDSL.select(AUTHENTICATIONS.AUTHENTICATION_EMAIL)
                .from(ONE_TIME_NOTIFICATIONS)
                .innerJoin(AUTHENTICATIONS)
                .on(AUTHENTICATIONS.USER_ID.eq(ONE_TIME_NOTIFICATIONS.DEST_USER_ID))
                .where(
                        ONE_TIME_NOTIFICATIONS.NOTIFICATION_IDENTIFIER.eq(notificationId)
                        .and(ONE_TIME_NOTIFICATIONS.NOTIFICATION_TYPE.eq(notificationType.getId()))
                )
        ).stream().map(r -> r.get(AUTHENTICATIONS.AUTHENTICATION_EMAIL)).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean hasUserReceivedNotification(long userId,
                                               @NotNull NotificationType notificationType,
                                               @NotNull String notificationId) {
        return sqlQuery.fetchFirst(
                PostgresDSL.select(ONE_TIME_NOTIFICATIONS.DB_ID)
                .from(ONE_TIME_NOTIFICATIONS)
                .where(
                        ONE_TIME_NOTIFICATIONS.NOTIFICATION_IDENTIFIER.eq(notificationId)
                        .and(ONE_TIME_NOTIFICATIONS.NOTIFICATION_TYPE.eq(notificationType.getId()))
                        .and(ONE_TIME_NOTIFICATIONS.DEST_USER_ID.eq(userId))
                )
                .limit(1)
        ).isPresent();
    }
}
