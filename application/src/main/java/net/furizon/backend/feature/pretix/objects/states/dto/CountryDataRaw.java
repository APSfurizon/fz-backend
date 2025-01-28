package net.furizon.backend.feature.pretix.objects.states.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.states.CountryData;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

@Data
public class CountryDataRaw {
    private final String name;

    private final String code;

    @JsonProperty("dial_code")
    @NotNull private final String phonePrefix;

    public CountryData toPhoneCountry() {
        var loc = Locale.of("", code);
        Map<String, String> translatedDescription = Map.of(
                Locale.ITALIAN.getLanguage(), loc.getDisplayCountry(Locale.ITALIAN),
                Locale.ENGLISH.getLanguage(), loc.getDisplayCountry(Locale.ENGLISH));
        return new CountryData(name, code, phonePrefix, translatedDescription);
    }

}
