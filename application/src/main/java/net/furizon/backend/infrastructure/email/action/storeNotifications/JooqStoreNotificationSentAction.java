package net.furizon.backend.infrastructure.email.action.storeNotifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.email.NotificationType;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import org.jetbrains.annotations.NotNull;
import org.jooq.util.postgres.PostgresDSL;
import org.springframework.stereotype.Component;

import java.util.Set;

import static net.furizon.jooq.generated.Tables.ONE_TIME_NOTIFICATIONS;

@Slf4j
@Component
@RequiredArgsConstructor
public class JooqStoreNotificationSentAction implements StoreNotificationSentAction {
    @NotNull
    private final SqlCommand command;

    @Override
    public void invoke(
            @NotNull Set<Long> userIds,
            @NotNull NotificationType notificationType,
            @NotNull String notificationId) {
        var q = PostgresDSL.insertInto(
                ONE_TIME_NOTIFICATIONS,
                ONE_TIME_NOTIFICATIONS.DEST_USER_ID,
                ONE_TIME_NOTIFICATIONS.NOTIFICATION_TYPE,
                ONE_TIME_NOTIFICATIONS.NOTIFICATION_IDENTIFIER
        );
        int notificationTypeId = notificationType.getId();
        for (var userId : userIds) {
            q = q.values(userId, notificationTypeId, notificationId);
        }
        command.execute(q);
    }
}
