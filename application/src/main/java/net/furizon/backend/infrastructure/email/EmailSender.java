package net.furizon.backend.infrastructure.email;

import jakarta.mail.MessagingException;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.springframework.mail.MailException;

public interface EmailSender {
    @NonBlocking
    void send(long userId, @NotNull String subject, @NotNull String title, @NotNull String mailBody, MailVarPair... vars);

    @NonBlocking
    void send(@NotNull UserEmailData emailData, @NotNull String subject, @NotNull String title, @NotNull String mailBody, MailVarPair... vars);

    @Blocking
    void send(MailRequest request) throws MessagingException, MailException;

    @NonBlocking
    void fireAndForget(MailRequest request);
}
