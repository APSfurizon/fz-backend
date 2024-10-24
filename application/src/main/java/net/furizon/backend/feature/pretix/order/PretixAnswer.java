package net.furizon.backend.feature.pretix.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class PretixAnswer {

    @JsonProperty("question")
    private final int questionId;

    @Nullable
    private final String answer;
}
