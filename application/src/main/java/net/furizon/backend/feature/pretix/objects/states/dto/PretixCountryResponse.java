package net.furizon.backend.feature.pretix.objects.states.dto;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.states.PhoneCountry;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class PretixCountryResponse {
    @NotNull
    private final List<PhoneCountry> data;

    public PretixCountryResponse(@NotNull final List<PretixState> data) {
        this.data = data.stream().map(k -> k instanceof PhoneCountry ? (PhoneCountry) k : null).toList();
    }
}
