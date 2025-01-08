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
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderService implements EmailSender {
    @NotNull private final UserFinder userFinder;

    @NotNull private final JavaMailSender mailSender;
    @NotNull private final AsyncTaskExecutor asyncTaskExecutor;
    @NotNull private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

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
                .to(mail)
                .subject(subject)
                .templateMessage(msg)
                .build()
        );
    }

    @Override
    public void send(MailRequest request) throws MessagingException, MailException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        mimeMessageHelper.setFrom(from);
        mimeMessageHelper.setTo(request.getTo());
        mimeMessageHelper.setSubject(request.getSubject());

        if (request.getTemplateMessage() != null) {
            final var message = request.getTemplateMessage();
            TemplateOutput output = new StringOutput();
            templateEngine.render(message.getTemplate(), message.getParams(), output);
            mimeMessageHelper.setText(output.toString(), true);
        } else if (request.getMessage() != null) {
            mimeMessageHelper.setText(request.getMessage(), false);
        } else {
            throw new RuntimeException("message or template is required");
        }

        mailSender.send(mimeMessage);
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
}
