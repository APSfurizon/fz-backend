package net.furizon.backend.feature.pretix.objects.checkins.dto.gadgets;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
class GadgetPermission {
    private final long permission;
    @NotNull
    private final List<Gadget> gadgets;
}
