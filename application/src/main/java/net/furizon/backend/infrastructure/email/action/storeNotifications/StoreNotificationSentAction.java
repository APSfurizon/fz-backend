package net.furizon.backend.infrastructure.email.action.storeNotifications;

import net.furizon.backend.infrastructure.email.NotificationType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface StoreNotificationSentAction {
    void invoke(
            @NotNull Set<Long> userIds,
            @NotNull NotificationType notificationType,
            @NotNull String notificationId);
}
