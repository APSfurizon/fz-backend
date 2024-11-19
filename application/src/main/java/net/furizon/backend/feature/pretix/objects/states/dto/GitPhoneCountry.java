package net.furizon.backend.feature.pretix.objects.states.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.states.PhoneCountry;
import org.jetbrains.annotations.NotNull;

@Data
public class GitPhoneCountry {
    private final String name;

    private final String code;

    @JsonProperty("dial_code")
    @NotNull private final String phonePrefix;

    public PhoneCountry toPhoneCountry() {
        return new PhoneCountry(name, code, phonePrefix);
    }

}
