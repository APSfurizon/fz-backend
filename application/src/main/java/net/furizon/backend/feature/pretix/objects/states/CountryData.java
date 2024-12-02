package net.furizon.backend.feature.pretix.objects.states;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
@EqualsAndHashCode(callSuper = true)
public class CountryData extends PretixState {
    @NotNull String phonePrefix;

    public CountryData(@NotNull String name, @NotNull String code, @NotNull String phonePrefix) {
        super(code, name);
        this.phonePrefix = phonePrefix;
    }
}
