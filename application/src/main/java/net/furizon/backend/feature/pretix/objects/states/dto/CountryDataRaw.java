package net.furizon.backend.feature.pretix.objects.states.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.states.CountryData;
import net.furizon.backend.infrastructure.localization.TranslationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class CountryDataRaw {
    private final String name;

    private final String code;

    @JsonProperty("dial_code")
    @NotNull private final String phonePrefix;

    public CountryData toPhoneCountry(@NotNull List<String> supportedLanguages) {
        var countryLocale = Locale.of("", code);

        Map<String, String> translatedDescription = supportedLanguages
                .stream()
                .map(language -> {
                    Locale locale = TranslationUtil.parseLocale(language);
                    return Map.entry(
                            locale.toString().toLowerCase().replace("_", "-"),
                        countryLocale.getDisplayCountry(locale)
                    );
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new CountryData(name, code, phonePrefix, translatedDescription);
    }

}
