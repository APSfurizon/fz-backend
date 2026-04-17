package net.furizon.backend.feature.pretix.objects.checkins.dto.gadgets;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class GadgetManager {
    @Getter
    @NotNull
    private final List<Gadget> gadgets = new ArrayList<>();
    @NotNull
    private final Map<String, Gadget> idToGadget = new HashMap<>();

    public GadgetManager(@NotNull Collection<Gadget> gadgets) {
        addAll(gadgets);
    }

    public GadgetManager(@NotNull GadgetManager gadgetManager) {
        addAll(gadgetManager.gadgets);
    }

    public @NotNull GadgetManager addAll(@Nullable GadgetManager gadgetManager) {
        if (gadgetManager == null) {
            return this;
        }
        return addAll(gadgetManager.gadgets);
    }

    public @NotNull GadgetManager addAll(@Nullable Collection<Gadget> gadgets) {
        if (gadgets == null) {
            return this;
        }
        gadgets.forEach(this::addGadget);
        return this;
    }

    public @NotNull GadgetManager addGadget(@NotNull Gadget gadget) {
        Gadget existing = idToGadget.get(gadget.getGadgetId());
        if (existing == null) {
            idToGadget.put(gadget.getGadgetId(), gadget);
            gadgets.add(gadget);
        } else {
            if (!gadget.isStackable() || !existing.isStackable()) {
                //If not stackable we don't increase gadget quantity
                existing.setStackable(false);
                return this;
            }
            existing.getGadgetNames().putAll(gadget.getGadgetNames());
            existing.setQuantity(existing.getQuantity() + gadget.getQuantity());
            existing.setShirt(existing.isShirt() || gadget.isShirt());
        }
        return this;
    }
}
