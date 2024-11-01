package net.furizon.backend.feature.pretix.order.dto;

import lombok.Data;
import net.furizon.backend.feature.pretix.order.PretixAnswer;

import java.util.List;

@Data
public class PushPretixAnswerRequest {
    private final List<PretixAnswer> answers;
}
