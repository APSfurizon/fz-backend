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

    //This is intended to be changeable
    //TODO deep copy method? Stark tvb <3
    @Nullable
    private String answer;
}
