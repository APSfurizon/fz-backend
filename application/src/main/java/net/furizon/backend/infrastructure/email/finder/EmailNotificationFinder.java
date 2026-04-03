package net.furizon.backend.infrastructure.email.finder;

import net.furizon.backend.infrastructure.email.NotificationType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface EmailNotificationFinder {
    @NotNull Set<Long> findUserIdsByNotification(@NotNull NotificationType notificationType,
                                                 @NotNull String notificationId);

    @NotNull Set<String> findUserEmailsByNotification(@NotNull NotificationType notificationType,
                                                      @NotNull String notificationId);

    boolean hasUserReceivedNotification(long userId,
                                        @NotNull NotificationType notificationType,
                                        @NotNull String notificationId);
}
