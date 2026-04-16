package net.furizon.backend.feature.pretix.objects.checkins.dto.gadgets;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Data
@Builder
public class Gadget {
    @NotNull private final Map<String, String> gadgetNames;
    @NotNull private final String gadgetId;


    private boolean stackable = true;
    private boolean shirt = false;
    private long quantity = 1L;

}
