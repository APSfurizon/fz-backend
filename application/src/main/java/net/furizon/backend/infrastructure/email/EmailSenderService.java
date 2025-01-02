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
    public void send(long userId, @NotNull String subject, @NotNull String title, @NotNull String mailBody, MailVarPair... vars) {
        UserEmailData data = userFinder.getMailDataForUser(userId);
        if (data == null) {
            log.error("Could not find mail data for user {}", userId);
            return;
        }

        for (MailVarPair pair : vars) {
            if (pair != null) {
                EmailVars var = pair.var();
                StringBuilder pre = new StringBuilder();
                StringBuilder post = new StringBuilder();
                for (EmailVars.Format format : var.getFormats()) {
                    switch (format) {
                        case ITALICS: {
                            pre.append("<i>");
                            post.insert(0, "</i>");
                            break;
                        }
                        case BOLD: {
                            pre.append("<b>");
                            post.insert(0, "</b>");
                            break;
                        }
                        case LINK: {
                            //Purposely do nothing
                            break;
                        }
                        default: throw new IllegalStateException("Unexpected value: " + format);
                    }
                }
                mailBody.replace(var.toString(), pre.toString() + pair.value() + post.toString());
            }
        }

        String mail = data.getEmail();
        log.info("Sending email to user {} ({}) with subject '{}' and title '{}'", userId, mail, subject, title);
        fireAndForget(
            MailRequest.builder()
                .to(mail)
                .subject(subject)
                .templateMessage(
                    TemplateMessage.of("old_template.jte")
                        .addParam("fursonaName", data.getFursonaName())
                        .addParam("title", title)
                        .addParam("body", mailBody)
                )
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
