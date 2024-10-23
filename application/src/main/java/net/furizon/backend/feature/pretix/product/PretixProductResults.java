package net.furizon.backend.feature.pretix.product;

/*
public Set<Integer> ticketIds = new HashSet<>();
public Map<Integer, Integer> dailyIds = new HashMap<>(); //map id -> day idx

public Set<Integer> membershipCardIds = new HashSet<>();

public Set<Integer> sponsorshipItemIds = new HashSet<>();
public Map<Integer, Sponsorship> sponsorshipVariationIds = new HashMap<>();

public Map<Integer, ExtraDays> extraDaysIds = new HashMap<>();

public Set<Integer> roomItemIds = new HashSet<>();
//map id -> (capacity, hotelName)
public Map<Integer, Pair<Integer, String>> roomVariationIds = new HashMap<>();
//map capacity/name -> room name TODO CHECK IF THIS WORKS
public Map<Pair<Integer, String>, String> roomNames = new HashMap<>();
*/

import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.springframework.data.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record PretixProductResults(
        Set<Integer> ticketItemIds,
        Map<Integer, Integer> dailyIdToDay,
        Set<Integer> membershipCardItemIds,
        Set<Integer> sponsorshipItemIds,
        Map<Integer, Sponsorship> sponsorshipIdToType,
        Map<Integer, ExtraDays> extraDaysIdToDay,
        Set<Integer> roomItemIds,
        Map<Integer, Pair<Integer, String>> roomIdToInfo,
        Map<Pair<Integer, String>, String> roomInfoToName) {
    public PretixProductResults() {
        this(
            new HashSet<>(),
            new HashMap<>(),
            new HashSet<>(),
            new HashSet<>(),
            new HashMap<>(),
            new HashMap<>(),
            new HashSet<>(),
            new HashMap<>(),
            new HashMap<>()
        );
    }
}
