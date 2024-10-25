package net.furizon.backend.feature.pretix.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class PretixAnswer {

    @JsonProperty("question")
    private final int questionId;

    @Nullable
    private String answer;
}
