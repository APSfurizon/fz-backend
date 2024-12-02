package net.furizon.backend.feature.pretix.objects.states;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class PretixState {
    @NotNull private final String code;
    @NotNull private final String name;
}
