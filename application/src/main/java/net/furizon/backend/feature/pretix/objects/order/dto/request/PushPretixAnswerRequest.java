package net.furizon.backend.feature.pretix.objects.order.dto.request;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.order.PretixAnswer;

import java.util.List;

@Data
public class PushPretixAnswerRequest {
    private final List<PretixAnswer> answers;
}
