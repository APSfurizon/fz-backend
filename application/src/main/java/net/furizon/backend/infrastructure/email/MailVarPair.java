package net.furizon.backend.infrastructure.email;

import net.furizon.backend.infrastructure.localization.model.TranslatableValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MailVarPair {
    @NotNull EmailVars var;
    @Nullable String value;
    @Nullable TranslatableValue translatableValue;

    private MailVarPair(@NotNull EmailVars var, @NotNull String value) {
        this.var = var;
        this.value = value;
    }

    private MailVarPair(@NotNull EmailVars var, @NotNull TranslatableValue translatableValue) {
        this.var = var;
        this.translatableValue = translatableValue;
    }

    public static MailVarPair of(@NotNull EmailVars var,
                                 @NotNull String value) {
        return new MailVarPair(var, value);
    }

    public static MailVarPair of(@NotNull EmailVars var,
                                 @NotNull TranslatableValue translatableValue) {
        return new MailVarPair(var, translatableValue);
    }

    public EmailVars getKey() {
        return var;
    }

    public Object getValue() {
        return value == null ? translatableValue : value;
    }

}
