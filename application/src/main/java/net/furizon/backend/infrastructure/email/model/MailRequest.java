package net.furizon.backend.infrastructure.email.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.MailVarPair;
import net.furizon.backend.infrastructure.localization.model.TranslatableValue;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Data
@Setter(AccessLevel.NONE)
@NoArgsConstructor
public class MailRequest {
    private List<Triple<@NotNull Locale, @NotNull String, @Nullable Long>> to;
    private TranslatableValue subject;
    private String message;
    private TemplateMessage templateMessage;
    @Nullable private String replyTo;

    public MailRequest to(@NotNull final List<Triple<@NotNull Locale, @NotNull String, @Nullable Long>> to) {
        this.to = to;
        return this;
    }
    public MailRequest to(@NotNull final Locale language, @NotNull final String to, @Nullable Long userId) {
        this.to = List.of(Triple.of(language, to, userId));
        return this;
    }
    public MailRequest subject(@NotNull final TranslatableValue subject) {
        this.subject = subject;
        return this;
    }
    public MailRequest subject(@PropertyKey(resourceBundle = "email.email") @NotNull final String subject,
                               Object... params) {
        this.subject = TranslatableValue.ofEmail(subject, params);
        return this;
    }
    public MailRequest message(@Nullable final String message) {
        this.message = message;
        return this;
    }
    public MailRequest templateMessage(@Nullable final TemplateMessage templateMessage) {
        this.templateMessage = templateMessage;
        return this;
    }
    public MailRequest templateMessage(@NotNull String templateName, @Nullable String fursonaName,
                                       MailVarPair... vars) {
        TemplateMessage msg = TemplateMessage.of(templateName);
        if (fursonaName != null) {
            msg = msg.addParam("fursonaName", fursonaName);
        }
        for (MailVarPair var : vars) {
            if (var != null) {
                msg = msg.addParam(var.getKey().getName(), var.getValue());
            }
        }
        this.templateMessage = msg;
        return this;
    }

    public MailRequest(@NotNull List<UserEmailData> emailData, @NotNull String templateName, MailVarPair... vars) {
        to = emailData.stream().map(ued -> Triple.of(ued.getLanguage(), ued.getEmail(), ued.getUserId())).toList();
        String fursonas = String.join(", ", emailData.stream().map(UserEmailData::getFursonaName).toList());
        templateMessage(templateName, fursonas, vars);
    }

    public MailRequest(@NotNull UserEmailData emailData, @NotNull String templateName, MailVarPair... vars) {
        to = List.of(Triple.of(emailData.getLanguage(), emailData.getEmail(), emailData.getUserId()));
        templateMessage(templateName, emailData.getFursonaName(), vars);
    }

    public MailRequest(@NotNull List<Long> userIds, @NotNull UserFinder finder, @NotNull String templateName,
                       MailVarPair... vars) {
        this(finder.getMailDataForUsers(userIds), templateName, vars);
    }

    public MailRequest(long userId, @NotNull UserFinder finder, @NotNull String templateName, MailVarPair... vars) {
        this(Objects.requireNonNull(finder.getMailDataForUser(userId)), templateName, vars);
    }

    public MailRequest(@NotNull List<Triple<@NotNull Locale, @NotNull String, @Nullable Long>> to,
                       @NotNull TranslatableValue subject,
                       @Nullable String message, @Nullable TemplateMessage templateMessage) {
        this.to = to;
        this.subject = subject;
        this.message = message;
        this.templateMessage = templateMessage;
    }

    public MailRequest replyTo(@Nullable String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public MailRequest addParam(@NotNull String key, @NotNull Object value) {
        this.templateMessage.addParam(key, value);
        return this;
    }
}
