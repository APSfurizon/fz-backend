package net.furizon.backend.infrastructure.email;

import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.email.model.TemplateMessage;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderService implements EmailSender {
    @NotNull
    private final UserFinder userFinder;

    @NotNull
    private final PermissionFinder permissionFinder;

    @NotNull
    private final JavaMailSender mailSender;

    @NotNull
    private final AsyncTaskExecutor asyncTaskExecutor;

    @NotNull
    private final TemplateEngine templateEngine;

    private final PrivateKey privateKey;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendToRole(@NotNull String roleInternalName, @NotNull String subject,
                           @NotNull String templateName, MailVarPair... vars) {

        //TODO FIX HANG
        /*List<Long> users = permissionFinder.getUsersWithRole(roleInternalName);
        for (Long user : users) {
            send(user, subject, templateName, vars);
        }*/
    }

    @Override
    public void sendToPermission(@NotNull Permission permission, @NotNull String subject,
                                 @NotNull String templateName, MailVarPair... vars) {
        //TODO FIX HANG
        /*List<Long> users = permissionFinder.getUsersWithPermission(permission);
        for (Long user : users) {
            send(user, subject, templateName, vars);
        }*/
    }

    @Override
    public void send(long userId, @NotNull String subject, @NotNull String templateName, MailVarPair... vars) {
        UserEmailData data = userFinder.getMailDataForUser(userId);
        if (data == null) {
            log.error("Could not find mail data for user {}", userId);
            return;
        }
        send(data, subject, templateName, vars);
    }

    @Override
    public void send(@NotNull UserEmailData emailData, @NotNull String subject, @NotNull String templateName,
                     MailVarPair... vars) {
        String mail = emailData.getEmail();
        log.info("Sending email to user {} ({}) with subject '{}'",
            emailData.getUserId(), mail, subject);


        TemplateMessage msg = TemplateMessage.of(templateName)
            .addParam("fursonaName", emailData.getFursonaName());

        for (MailVarPair var : vars) {
            if (var != null) {
                msg = msg.addParam(var.var().getName(), var.value());
            }
        }

        fireAndForget(
            MailRequest.builder()
                .to(List.of(mail))
                .subject(subject)
                .templateMessage(msg)
                .build()
        );
    }

    @Override
    public void send(@NotNull MailRequest request) throws MessagingException, MailException {
        mailSender.send(
            transformMailRequestToMimeMessages(request)
        );
    }

    @Override
    public void sendMany(MailRequest... requests) throws MessagingException, MailException {
        mailSender.send(
            transformMailRequestsToMimeMessages(requests)
        );
    }

    @Override
    public void fireAndForget(MailRequest request) {
        asyncTaskExecutor.execute(() -> {
            try {
                log.debug("Sending email in async mode");
                send(request);
            } catch (MessagingException ex) {
                log.error("Couldn't send message", ex);
            }
        });
    }

    @Override
    public void fireAndForgetMany(MailRequest... requests) {
        asyncTaskExecutor.execute(() -> {
            try {
                log.debug("Sending emails in async mode");
                sendMany(requests);
            } catch (MessagingException ex) {
                log.error("Couldn't send message", ex);
            }
        });
    }

    private MimeMessage[] transformMailRequestsToMimeMessages(@NotNull MailRequest... requests) {
        return Arrays
            .stream(requests)
            .map(this::transformMailRequestToMimeMessages)
            .flatMap(Arrays::stream)
            .toArray(MimeMessage[]::new);
    }

    private MimeMessage[] transformMailRequestToMimeMessages(@NotNull MailRequest request) {
        final var isTemplateMessage = request.getTemplateMessage() != null;
        final var bodyMessage = isTemplateMessage ? convertTemplateToString(request) : request.getMessage();
        if (bodyMessage == null) {
            throw new RuntimeException("message is required");
        }

        return request
            .getTo()
            .stream()
            .map(to -> buildMimeMessage(to, request.getSubject(), bodyMessage, isTemplateMessage))
            .toArray(MimeMessage[]::new);
    }

    @NotNull
    private String convertTemplateToString(@NotNull MailRequest request) {
        final var message = request.getTemplateMessage();
        if (message == null) {
            throw new RuntimeException("template is required for this action");
        }

        TemplateOutput output = new StringOutput();
        templateEngine.render(message.getTemplate(), message.getParams(), output);
        return output.toString();
    }

    @NotNull
    private MimeMessage buildMimeMessage(
        @NotNull String to,
        @NotNull String subject,
        @NotNull String body,
        boolean isTemplateMessage
    ) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, isTemplateMessage);

            return mimeMessage;
        } catch (MessagingException exception) {
            throw new RuntimeException(exception);
        }
    }
}
