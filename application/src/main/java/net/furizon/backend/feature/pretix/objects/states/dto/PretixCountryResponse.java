package net.furizon.backend.feature.pretix.objects.states.dto;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.states.CountryData;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class PretixCountryResponse {
    @NotNull
    private final List<CountryData> data;

    public PretixCountryResponse(@NotNull final List<PretixState> data) {
        this.data = data.stream().map(k -> k instanceof CountryData ? (CountryData) k : null).toList();
    }
}
