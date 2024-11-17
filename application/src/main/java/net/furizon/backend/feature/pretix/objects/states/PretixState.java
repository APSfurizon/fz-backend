package net.furizon.backend.feature.pretix.objects.states;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PretixState {
    private final String code;
    private final String name;
}
