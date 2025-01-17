package net.furizon.backend.feature.pretix.objects.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@AllArgsConstructor
public class PretixAnswer {
    @JsonProperty("question")
    private final long questionId;

    @Nullable
    private String answer;

    @NotNull
    @JsonProperty("options")
    private List<Long> optionsId;
    @NotNull
    @JsonProperty("option_identifiers")
    private List<String> optionIdentifiers;
}
