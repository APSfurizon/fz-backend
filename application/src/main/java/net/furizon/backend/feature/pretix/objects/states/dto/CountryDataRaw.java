package net.furizon.backend.feature.pretix.objects.states.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.states.CountryData;
import org.jetbrains.annotations.NotNull;

@Data
public class CountryDataRaw {
    private final String name;

    private final String code;

    @JsonProperty("dial_code")
    @NotNull private final String phonePrefix;

    public CountryData toPhoneCountry() {
        return new CountryData(name, code, phonePrefix);
    }

}
