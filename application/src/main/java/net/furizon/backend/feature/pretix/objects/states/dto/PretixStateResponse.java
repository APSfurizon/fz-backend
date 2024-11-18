package net.furizon.backend.feature.pretix.objects.states.dto;

import lombok.Data;
import net.furizon.backend.feature.pretix.objects.states.PretixState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class PretixStateResponse {
    @NotNull
    private final List<PretixState> data;
}
