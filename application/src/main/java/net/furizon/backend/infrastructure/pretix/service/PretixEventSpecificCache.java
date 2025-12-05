package net.furizon.backend.infrastructure.pretix.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.infrastructure.pretix.model.CacheItemTypes;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

class PretixEventSpecificCache {

    //Contains tickets, memberships, sponsors, rooms
    @NotNull
    final Cache<CacheItemTypes, Set<Long>> itemIdsCache = Caffeine.newBuilder().build();

    //Questions
    @NotNull
    final AtomicReference<Long> questionUserId = new AtomicReference<>(-1L);
    @NotNull
    final AtomicReference<Long> questionDuplicateData = new AtomicReference<>(-1L);
    @NotNull
    final AtomicReference<Long> questionUserNotes = new AtomicReference<>(-1L);
    @NotNull
    final Cache<String, Long> questionIdentifierToId = Caffeine.newBuilder().build();

    //Sponsors
    @NotNull
    final Cache<Sponsorship, Set<Long>> sponsorshipTypeToIds = Caffeine.newBuilder().build();

    //Extra days
    //map (capacity, hotelName, roomName) -> earlyExtraDays item id
    @NotNull
    final Cache<HotelCapacityPair, Long> roomIdToEarlyExtraDayItemId = Caffeine.newBuilder().build();
    //map (capacity, hotelName, roomName) -> lateExtraDays item id
    @NotNull
    final Cache<HotelCapacityPair, Long> roomIdToLateExtraDayItemId = Caffeine.newBuilder().build();

    //Rooms
    //map capacity -> List[id of room items with that capacity]
    @NotNull
    final Cache<Short, List<Long>> roomCapacityToItemId = Caffeine.newBuilder().build();

    //Board
    //map (capacity, hotelName, roomName) -> board main item id
    @NotNull
    final Cache<HotelCapacityPair, Long> boardCapacityToItemId  = Caffeine.newBuilder().build();
    //map (capacity, hotelName, roomName) -> half board variation id
    @NotNull
    final Cache<HotelCapacityPair, Long> halfBoardCapacityToVariationId  = Caffeine.newBuilder().build();
    //map (capacity, hotelName, roomName) -> full board variation id
    @NotNull
    final Cache<HotelCapacityPair, Long> fullBoardCapacityToVariationId  = Caffeine.newBuilder().build();

    public void invalidate() {
        //General
        itemIdsCache.invalidateAll();

        //Questions
        questionUserId.set(-1L);
        questionUserNotes.set(-1L);
        questionDuplicateData.set(-1L);
        questionIdentifierToId.invalidateAll();

        //Sponsors
        sponsorshipTypeToIds.invalidateAll();

        //Rooms
        roomIdToEarlyExtraDayItemId.invalidateAll();
        roomIdToLateExtraDayItemId.invalidateAll();
        roomCapacityToItemId.invalidateAll();

        //Board
        boardCapacityToItemId.invalidateAll();
        halfBoardCapacityToVariationId.invalidateAll();
        fullBoardCapacityToVariationId.invalidateAll();
    }
}
