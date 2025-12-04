package net.furizon.backend.infrastructure.email;

import jakarta.mail.MessagingException;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.localization.model.TranslatableValue;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.springframework.mail.MailException;

import java.util.List;

public interface EmailSender {
    @NonBlocking
    List<MailRequest> prepareForRole(
        @NotNull String roleInternalName,
        @NotNull TranslatableValue subject,
        @NotNull String templateName,
        MailVarPair... vars
    );

    @NonBlocking
    List<MailRequest> prepareForPermission(
        @NotNull Permission permission,
        @NotNull TranslatableValue subject,
        @NotNull String templateName,
        MailVarPair... vars
    );

    @NonBlocking
    List<MailRequest> prepareForUsers(
            @NotNull List<Long> users,
            @NotNull TranslatableValue subject,
            @NotNull String templateName,
            MailVarPair... vars
    );

    @NonBlocking
    void prepareAndSendForRole(
            @NotNull String roleInternalName,
            @NotNull TranslatableValue subject,
            @NotNull String templateName,
            MailVarPair... vars
    );
    @NonBlocking
    void prepareAndSendForPermission(
            @NotNull Permission permission,
            @NotNull TranslatableValue subject,
            @NotNull String templateName,
            MailVarPair... vars
    );
    @NonBlocking
    void prepareAndSendForUsers(
            @NotNull List<Long> users,
            @NotNull TranslatableValue subject,
            @NotNull String templateName,
            MailVarPair... vars
    );

    @Blocking
    void send(MailRequest request) throws MessagingException, MailException;
    @Blocking
    void sendMany(MailRequest... requests) throws MessagingException, MailException;
    @Blocking
    void sendMany(List<MailRequest> requests) throws MessagingException, MailException;

    @NonBlocking
    void fireAndForget(MailRequest request);
    @NonBlocking
    void fireAndForgetMany(MailRequest... requests);
    @NonBlocking
    void fireAndForgetMany(List<MailRequest> requests);
}
