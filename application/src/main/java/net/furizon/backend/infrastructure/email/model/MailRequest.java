package net.furizon.backend.infrastructure.email.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.furizon.backend.feature.user.dto.UserEmailData;
import net.furizon.backend.feature.user.finder.UserFinder;
import net.furizon.backend.infrastructure.email.MailVarPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Data
@Setter(AccessLevel.NONE)
@NoArgsConstructor
public class MailRequest {
    private List<String> to;
    private String subject;
    private String message;
    private TemplateMessage templateMessage;

    public MailRequest to(@NotNull final List<String> to) {
        this.to = to;
        return this;
    }
    public MailRequest to(@NotNull final String to) {
        this.to = List.of(to);
        return this;
    }
    public MailRequest subject(@NotNull final String subject) {
        this.subject = subject;
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
                msg = msg.addParam(var.var().getName(), var.value());
            }
        }
        this.templateMessage = msg;
        return this;
    }

    public MailRequest(@NotNull List<UserEmailData> emailData, @NotNull String templateName, MailVarPair... vars) {
        to = emailData.stream().map(UserEmailData::getEmail).toList();
        String fursonas = String.join(", ", emailData.stream().map(UserEmailData::getFursonaName).toList());
        templateMessage(templateName, fursonas, vars);
    }

    public MailRequest(@NotNull UserEmailData emailData, @NotNull String templateName, MailVarPair... vars) {
        to = List.of(emailData.getEmail());
        templateMessage(templateName, emailData.getFursonaName(), vars);
    }

    public MailRequest(@NotNull List<Long> userIds, @NotNull UserFinder finder, @NotNull String templateName,
                       MailVarPair... vars) {
        this(finder.getMailDataForUsers(userIds), templateName, vars);
    }

    public MailRequest(long userId, @NotNull UserFinder finder, @NotNull String templateName, MailVarPair... vars) {
        this(Objects.requireNonNull(finder.getMailDataForUser(userId)), templateName, vars);
    }

    public MailRequest(@NotNull List<String> to, @NotNull String subject,
                       @Nullable String message, @Nullable TemplateMessage templateMessage) {
        this.to = to;
        this.subject = subject;
        this.message = message;
        this.templateMessage = templateMessage;
    }

    public MailRequest addParam(@NotNull String key, @NotNull Object value) {
        this.templateMessage.addParam(key, value);
        return this;
    }
}
