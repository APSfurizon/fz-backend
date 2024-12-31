package net.furizon.backend.infrastructure.email;

import jakarta.mail.MessagingException;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;
import org.springframework.mail.MailException;

public interface EmailSender {
    @Blocking
    void send(MailRequest request) throws MessagingException, MailException;

    @NonBlocking
    void fireAndForget(MailRequest request);
}
