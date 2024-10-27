package net.furizon.backend.feature.pretix.product;

import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.springframework.data.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public record PretixProductResults(
        Set<Integer> ticketItemIds,
        Map<Integer, Integer> dailyIdToDay,
        Set<Integer> membershipCardItemIds,
        Set<Integer> sponsorshipItemIds,
        Map<Integer, Sponsorship> sponsorshipIdToType,
        Map<Integer, ExtraDays> extraDaysIdToDay,
        Set<Integer> roomItemIds,
        Map<Integer, HotelCapacityPair> roomIdToInfo,
        Map<HotelCapacityPair, Map<String, String>> roomInfoToNames) {
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
            new HashMap<>()
        );
    }
}
