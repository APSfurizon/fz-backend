package net.furizon.backend.infrastructure.email;

import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.localization.model.TranslatableValue;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.templating.JteContext;
import net.furizon.backend.infrastructure.templating.JteLocalizer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @NotNull
    private final JteLocalizer jteLocalizer;

    @NotNull
    private final TranslationService translationService;

    @Value("${spring.mail.username}")
    private String from;
    @Value("${spring.mail.subject-prepend-text}")
    private String subjectPrependText;

    @Override
    public List<MailRequest> prepareForRole(@NotNull String roleInternalName, @NotNull TranslatableValue subject,
                           @NotNull String templateName, MailVarPair... vars) {

        List<Long> users = permissionFinder.getUsersWithRoleInternalName(roleInternalName);
        return prepareForUsers(users, subject, templateName, vars);
    }

    @Override
    public List<MailRequest> prepareForPermission(@NotNull Permission permission, @NotNull TranslatableValue subject,
                                 @NotNull String templateName, MailVarPair... vars) {
        List<Long> users = permissionFinder.getUsersWithPermission(permission);
        return prepareForUsers(users, subject, templateName, vars);
    }

    @Override
    public List<MailRequest> prepareForUsers(@NotNull List<Long> users, @NotNull TranslatableValue subject,
                                             @NotNull String templateName, MailVarPair... vars) {
        List<MailRequest> reqs = new ArrayList<>(users.size() + 8);
        for (Long user : users) {
            reqs.add(new MailRequest(user, userFinder, templateName, vars).subject(subject));
        }
        return reqs;
    }

    @Override
    public void prepareAndSendForRole(@NotNull String roleInternalName, @NotNull TranslatableValue subject,
                                      @NotNull String templateName, MailVarPair... vars) {
        fireAndForgetMany(prepareForRole(roleInternalName, subject, templateName, vars));
    }
    @Override
    public void prepareAndSendForPermission(@NotNull Permission permission, @NotNull TranslatableValue subject,
                                            @NotNull String templateName, MailVarPair... vars) {
        fireAndForgetMany(prepareForPermission(permission, subject, templateName, vars));
    }
    @Override
    public void prepareAndSendForUsers(@NotNull List<Long> users, @NotNull TranslatableValue subject,
                                       @NotNull String templateName, MailVarPair... vars) {
        fireAndForgetMany(prepareForUsers(users, subject, templateName, vars));
    }


    @Override
    public void send(@NotNull MailRequest request) throws MessagingException, MailException {
        sendMany(request);
    }

    @Override
    public void sendMany(MailRequest... requests) throws MessagingException, MailException {
        if (requests.length == 0) {
            return;
        }
        log.info("Sending emails: {}", Arrays.toString(requests));
        for (MailRequest mr : requests) {
            if (mr.getSubject() != null) {
                mr.subject(mr.getSubject());
            }
        }
        mailSender.send(transformMailRequestsToMimeMessages(requests));
    }

    @Override
    public void sendMany(List<MailRequest> requests) throws MessagingException, MailException {
        if (requests.isEmpty()) {
            return;
        }
        mailSender.send(transformMailRequestsToMimeMessages(toArray(requests)));
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
        if (requests.length == 0) {
            return;
        }
        asyncTaskExecutor.execute(() -> {
            try {
                log.debug("Sending emails in async mode");
                sendMany(requests);
            } catch (MessagingException ex) {
                log.error("Couldn't send message", ex);
            }
        });
    }

    @Override
    public void fireAndForgetMany(List<MailRequest> requests) {
        if (requests.isEmpty()) {
            return;
        }
        fireAndForgetMany(toArray(requests));
    }

    private MimeMessage[] transformMailRequestsToMimeMessages(@NotNull MailRequest... requests) {
        return Arrays.stream(requests)
            .filter(Objects::nonNull)
            .map(this::transformMailRequestToMimeMessages)
            .flatMap(Arrays::stream)
            .toArray(MimeMessage[]::new);
    }

    private MimeMessage[] transformMailRequestToMimeMessages(@NotNull MailRequest request) {
        final var isTemplateMessage = request.getTemplateMessage() != null;
        // Group translated messages by locale
        final Map<Locale, String> translatedBody = request.getTo().stream().collect(Collectors.toMap(
                Pair::getLeft,
                pair -> {
                    final var bodyMessage = isTemplateMessage
                            ? translationService.withLocale(pair.getLeft(), () -> convertTemplateToString(request))
                            : request.getMessage();
                    if (bodyMessage == null) {
                        throw new RuntimeException("message is required for locale %s"
                                .formatted(pair.getLeft().getDisplayLanguage()));
                    }
                    return bodyMessage;
                }
        ));
        // Builds the MimeMessage with translated data for each recipient
        return request.getTo().stream()
            .map(to -> buildMimeMessage(to.getRight(),
                    translationService.email(request.getSubject().getKey(),
                            to.getLeft(),
                            request.getSubject().getParams()),
                    translatedBody.get(to.getLeft()),
                    isTemplateMessage))
            .toArray(MimeMessage[]::new);
    }

    @NotNull
    private String convertTemplateToString(@NotNull MailRequest request) {
        final var message = request.getTemplateMessage();
        if (message == null) {
            throw new RuntimeException("template is required for this action");
        }

        TemplateOutput output = new StringOutput();
        JteContext.init(this.jteLocalizer);
        templateEngine.render(message.getTemplate(), message.getParams(), output);
        JteContext.dispose();
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

            String subjectPrepend = subjectPrependText == null ? "" : subjectPrependText;

            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to); //TODO check if email leak is happening
            mimeMessageHelper.setSubject(subjectPrepend + subject);
            mimeMessageHelper.setText(body, isTemplateMessage);

            return mimeMessage;
        } catch (MessagingException exception) {
            throw new RuntimeException(exception);
        }
    }

    private @NotNull MailRequest[] toArray(@NotNull List<MailRequest> requests) {
        return requests.toArray(new MailRequest[requests.size()]);
    }
}
