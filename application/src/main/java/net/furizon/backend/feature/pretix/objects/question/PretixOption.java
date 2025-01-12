package net.furizon.backend.feature.pretix.objects.question;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Data
public class PretixOption {
    private final long id;

    @NotNull
    private final String identifier;

    @NotNull
    private final Map<String, String> answer;

    private final long position;
}
