package net.furizon.backend.feature.pretix.objects.product;

import net.furizon.backend.infrastructure.pretix.model.Board;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    @NotNull Map<Sponsorship, Set<Long>> sponsorshipTypeToIds,
    @NotNull Map<Long, ExtraDays> extraDaysIdToDay,
    @NotNull Set<Long> roomItemIds,
    @NotNull Set<Long> extraFursuitsItemIds,
    @NotNull Map<Long, HotelCapacityPair> roomIdToInfo,
    @NotNull Map<Long, Long> itemIdToPrice,
    @NotNull Map<Long, Long> variationIdToPrice,
    @NotNull Map<Long, Long> variationIdToFatherItemId,
    @NotNull Map<Long, Map<String, String>> itemIdToNames,
    @NotNull Map<Long, Map<String, String>> variationIdToNames,
    @NotNull Set<Long> noRoomItemIds,
    @NotNull Map<HotelCapacityPair, Long> earlyDaysItemId,
    @NotNull Map<HotelCapacityPair, Long> lateDaysItemId,
    @NotNull Map<Short, List<Long>> capacityToRoomItemIds,
    @NotNull Set<Long> tempAddons,
    @NotNull Set<Long> tempItems,
    @NotNull Map<Long, List<PretixProductBundle>> itemIdToBundle,
    @NotNull Set<Long> boardItemIds,
    @NotNull Map<HotelCapacityPair, Long> boardCapacityToItemId,
    @NotNull Map<HotelCapacityPair, Long> halfBoardCapacityToVariationId,
    @NotNull Map<HotelCapacityPair, Long> fullBoardCapacityToVariationId,
    @NotNull Map<Long, Board> boardVariationIdToType,
    @NotNull Map<Long, Boolean> isInternalItem,
    @NotNull Map<Long, Boolean> isInternalVariation
) {
    public PretixProductResults() {
        this(
            new TreeSet<>(),
            new TreeMap<>(),
            new TreeSet<>(),
            new TreeSet<>(),
            new TreeMap<>(),
            new TreeMap<>(),
            new TreeMap<>(),
            new TreeSet<>(),
            new TreeSet<>(),
            new TreeMap<>(),
            new TreeMap<>(),
            new TreeMap<>(),
            new TreeMap<>(),
            new TreeMap<>(),
            new TreeMap<>(),
            new TreeSet<>(),
            new HashMap<>(),
            new HashMap<>(),
            new TreeMap<>(),
            new HashSet<>(),
            new HashSet<>(),
            new TreeMap<>(),
            new TreeSet<>(),
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>(),
            new TreeMap<>(),
            new HashMap<>(),
            new HashMap<>()
        );
    }
}
