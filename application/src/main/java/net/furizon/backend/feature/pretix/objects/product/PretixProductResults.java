package net.furizon.backend.feature.pretix.objects.product;

import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public record PretixProductResults(
    @NotNull Set<Integer> ticketItemIds,
    @NotNull Map<Integer, Integer> dailyIdToDay,
    @NotNull Set<Integer> membershipCardItemIds,
    @NotNull Set<Integer> sponsorshipItemIds,
    @NotNull Map<Integer, Sponsorship> sponsorshipIdToType,
    @NotNull Map<Integer, ExtraDays> extraDaysIdToDay,
    @NotNull Set<Integer> roomItemIds,
    @NotNull Map<Integer, HotelCapacityPair> roomIdToInfo,
    @NotNull Map<HotelCapacityPair, Map<String, String>> roomInfoToNames,
    @NotNull Set<Integer> noRoomVariationIds
) {
    public PretixProductResults() {
        this(
            new TreeSet<>(),
            new TreeMap<>(),
            new TreeSet<>(),
            new TreeSet<>(),
            new TreeMap<>(),
            new TreeMap<>(),
            new TreeSet<>(),
            new TreeMap<>(),
            new HashMap<>(),
            new TreeSet<>()
        );
    }
}
