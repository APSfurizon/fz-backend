package net.furizon.backend.feature.pretix.objects.states.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class GitCountry {
    @NotNull
    private final String name;
    @NotNull
    @JsonProperty("alpha-2")
    private final String code;

    public PretixState toState() {
        return new PretixState(name, code);
    }
}
