package net.furizon.backend.infrastructure.email;

import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.action.storeNotifications.StoreNotificationSentAction;
import net.furizon.backend.infrastructure.email.finder.EmailNotificationFinder;
import net.furizon.backend.infrastructure.email.model.MailRequest;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.localization.model.TranslatableValue;
import net.furizon.backend.infrastructure.security.permissions.Permission;
import net.furizon.backend.infrastructure.security.permissions.finder.PermissionFinder;
import net.furizon.backend.infrastructure.templating.JteContext;
import net.furizon.backend.infrastructure.templating.JteLocalizer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
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
    private final EmailNotificationFinder emailNotificationFinder;

    @NotNull
    private final StoreNotificationSentAction storeNotificationSentAction;

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
    public void prepareAndSendNotificationForRole(
            @NotNull NotificationType notificationType, @NotNull String notificationIdentifier,
            @NotNull String roleInternalName, @NotNull TranslatableValue subject,
            @NotNull String templateName, MailVarPair... vars) {
        fireAndForgetMany(
            filterUserForNotification(
                prepareForRole(roleInternalName, subject, templateName, vars),
                notificationType,
                notificationIdentifier
            ),
            (mime, reqs) -> storeNotificationSent(
                    notificationType,
                    notificationIdentifier,
                    reqs
            )
        );
    }
    @Override
    public void prepareAndSendNotificationForPermission(
            @NotNull NotificationType notificationType, @NotNull String notificationIdentifier,
            @NotNull Permission permission, @NotNull TranslatableValue subject,
            @NotNull String templateName, MailVarPair... vars) {
        fireAndForgetMany(
            filterUserForNotification(
                prepareForPermission(permission, subject, templateName, vars),
                notificationType,
                notificationIdentifier
            ),
            (mime, reqs) -> storeNotificationSent(
                    notificationType,
                    notificationIdentifier,
                    reqs
            )
        );
    }
    @Override
    public void prepareAndSendNotificationForUsers(
            @NotNull NotificationType notificationType, @NotNull String notificationIdentifier,
            @NotNull List<Long> users, @NotNull TranslatableValue subject,
            @NotNull String templateName, MailVarPair... vars) {
        fireAndForgetMany(
            filterUserForNotification(
                prepareForUsers(users, subject, templateName, vars),
                notificationType,
                notificationIdentifier
            ),
            (mime, reqs) -> storeNotificationSent(
                    notificationType,
                    notificationIdentifier,
                    reqs
            )
        );
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
        sendMany(null, requests);
    }

    @Override
    public void sendMany(@Nullable BiConsumer<MimeMessage[], MailRequest[]> callback,
                         MailRequest... requests) throws MessagingException, MailException {
        if (requests.length == 0) {
            return;
        }
        log.info("Sending emails: {}", Arrays.toString(requests));
        for (MailRequest mr : requests) {
            if (mr.getSubject() != null) {
                mr.subject(mr.getSubject());
            }
        }
        var mimeMessages = transformMailRequestsToMimeMessages(requests);
        mailSender.send(mimeMessages);
        if (callback != null) {
            callback.accept(mimeMessages, requests);
        }
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
        fireAndForgetMany(null, requests);
    }
    @Override
    public void fireAndForgetMany(@Nullable BiConsumer<MimeMessage[], MailRequest[]> callback,
                                  MailRequest... requests) {
        if (requests.length == 0) {
            return;
        }
        asyncTaskExecutor.execute(() -> {
            try {
                log.debug("Sending emails in async mode");
                sendMany(callback, requests);
            } catch (MessagingException ex) {
                log.error("Couldn't send message", ex);
            }
        });
    }

    @Override
    public void fireAndForgetMany(List<MailRequest> requests) {
        fireAndForgetMany(requests, null);
    }
    @Override
    public void fireAndForgetMany(List<MailRequest> requests,
                                  @Nullable BiConsumer<MimeMessage[], MailRequest[]> callback) {
        if (requests.isEmpty()) {
            return;
        }
        fireAndForgetMany(callback, toArray(requests));
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
                Triple::getLeft,
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
            .map(to -> buildMimeMessage(
                    to.getMiddle(),
                    translationService.email(request.getSubject().getKey(),
                            to.getLeft(),
                            request.getSubject().getParams()),
                    translatedBody.get(to.getLeft()),
                    isTemplateMessage
                )
            )
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
        // Translate parameters
        Map<String, Object> params = message.getParams().entrySet().stream().map((entry) -> {
            final Object value;
            if (entry.getValue() instanceof TranslatableValue translatableValue) {
                value = translationService.translateFallback(translatableValue.getKey(),
                        translatableValue.getKey(),
                        translatableValue.getParams());
            } else {
                value = entry.getValue();
            }
            return Pair.of(entry.getKey(), value);
        }).collect(Collectors.toMap(Pair::getKey, Pair::getValue, (x, y) -> y));
        templateEngine.render(message.getTemplate(), params, output);
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

    private @NotNull List<MailRequest> filterUserForNotification(@NotNull List<MailRequest> requests,
                                                                 @NotNull NotificationType notificationType,
                                                                 @NotNull String notificationIdentifier) {
        Set<Long> notificationAlreadyReceived = emailNotificationFinder.findUserIdsByNotification(
                notificationType,
                notificationIdentifier
        );
        List<MailRequest> out = new ArrayList<>(requests.size());
        for (MailRequest request : requests) {
            List<Triple<@NotNull Locale, @NotNull String, @Nullable Long>> to = new ArrayList<>(request.getTo());
            boolean shouldRemoveAll = true;
            boolean hasModified = false;
            for (var user : request.getTo()) {
                if (notificationAlreadyReceived.contains(user.getRight())) {
                    to.remove(user);
                    hasModified = true;
                } else {
                    shouldRemoveAll = false;
                }
            }
            if (shouldRemoveAll) {
                continue;
            }
            if (hasModified) {
                out.add(new MailRequest(to, request.getSubject(), request.getMessage(), request.getTemplateMessage()));
            } else {
                out.add(request);
            }
        }
        return out;
    }

    private void storeNotificationSent(@NotNull NotificationType notificationType,
                                       @NotNull String notificationIdentifier,
                                       MailRequest... requests) {
        storeNotificationSentAction.invoke(
                Arrays.stream(requests)
                        .map(MailRequest::getTo)
                        .flatMap(Collection::stream)
                        .map(Triple::getRight)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()),
                notificationType,
                notificationIdentifier
        );
    }
}
