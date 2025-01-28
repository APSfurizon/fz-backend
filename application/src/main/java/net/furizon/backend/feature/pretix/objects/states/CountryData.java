package net.furizon.backend.feature.pretix.objects.states;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
public class CountryData extends PretixState {
    @NotNull String phonePrefix;
    @Nullable Map<String, String> translatedDescription;

    public CountryData(@NotNull String name, @NotNull String code, @NotNull String phonePrefix,
                       @Nullable Map<String, String> translatedDescription) {
        super(code, name);
        this.phonePrefix = phonePrefix;
        this.translatedDescription = translatedDescription;
    }
}
