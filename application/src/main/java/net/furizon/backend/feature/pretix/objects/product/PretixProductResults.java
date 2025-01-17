package net.furizon.backend.feature.pretix.objects.product;

import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public record PretixProductResults(
    @NotNull Set<Long> ticketItemIds,
    @NotNull Map<Long, Integer> dailyIdToDay,
    @NotNull Set<Long> membershipCardItemIds,
    @NotNull Set<Long> sponsorshipItemIds,
    @NotNull Map<Long, Sponsorship> sponsorshipIdToType,
    @NotNull Map<Long, ExtraDays> extraDaysIdToDay,
    @NotNull Set<Long> roomItemIds,
    @NotNull Set<Long> extraFursuitsItemIds,
    @NotNull Map<Long, HotelCapacityPair> roomIdToInfo,
    @NotNull Map<Long, Long> itemIdToPrice,
    @NotNull Map<Long, Map<String, String>> roomPretixItemIdToNames,
    @NotNull Set<Long> noRoomItemIds,
    @NotNull Map<HotelCapacityPair, Long> earlyDaysItemId,
    @NotNull Map<HotelCapacityPair, Long> lateDaysItemId,
    @NotNull Set<Long> tempAddons,
    @NotNull Set<Long> tempItems
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
            new TreeSet<>(),
            new TreeMap<>(),
            new HashMap<>(),
            new HashMap<>(),
            new TreeSet<>(),
            new HashMap<>(),
            new HashMap<>(),
            new HashSet<>(),
            new HashSet<>()
        );
    }
}
