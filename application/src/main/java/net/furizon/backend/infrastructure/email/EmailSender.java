package net.furizon.backend.infrastructure.email;

import jakarta.mail.MessagingException;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.springframework.mail.MailException;

public interface EmailSender {
    @NonBlocking
    void sendToRole(
        @NotNull String roleInternalName,
        @NotNull String subject,
        @NotNull String templateName,
        MailVarPair... vars
    );

    @NonBlocking
    void sendToPermission(
        @NotNull Permission permission,
        @NotNull String subject,
        @NotNull String templateName,
        MailVarPair... vars
    );

    @NonBlocking
    void send(long userId, @NotNull String subject, @NotNull String templateName, MailVarPair... vars);

    @NonBlocking
    void send(@NotNull UserEmailData emailData, @NotNull String subject, @NotNull String templateName,
              MailVarPair... vars);

    @Blocking
    void send(MailRequest request) throws MessagingException, MailException;

    @Blocking
    void sendMany(MailRequest... requests) throws MessagingException, MailException;

    @NonBlocking
    void fireAndForget(MailRequest request);

    @NonBlocking
    void fireAndForgetMany(MailRequest... requests);
}
