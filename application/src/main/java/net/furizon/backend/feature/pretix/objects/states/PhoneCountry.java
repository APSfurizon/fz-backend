package net.furizon.backend.feature.pretix.objects.states;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
@EqualsAndHashCode(callSuper = true)
public class PhoneCountry extends PretixState {
    @NotNull String phonePrefix;

    public PhoneCountry(@NotNull String name, @NotNull String code, @NotNull String phonePrefix) {
        super(name, code);
        this.phonePrefix = phonePrefix;
    }
}
