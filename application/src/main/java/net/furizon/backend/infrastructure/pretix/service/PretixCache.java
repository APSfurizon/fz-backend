package net.furizon.backend.infrastructure.pretix.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import net.furizon.backend.feature.pretix.objects.product.HotelCapacityPair;
import net.furizon.backend.feature.pretix.objects.product.PretixProductBundle;
import net.furizon.backend.feature.pretix.objects.question.PretixOption;
import net.furizon.backend.feature.pretix.objects.quota.PretixQuota;
import net.furizon.backend.infrastructure.pretix.model.Board;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import net.furizon.backend.infrastructure.pretix.model.QuestionType;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

class PretixCache {

    //Event Struct
    //map id -> price * 100
    @NotNull
    final Cache<Long, Long> itemIdToPrice = Caffeine.newBuilder().build();
    //map variation id -> price * 100
    @NotNull
    final Cache<Long, Long> variationIdToPrice = Caffeine.newBuilder().build();
    //map variation id -> father item id
    @NotNull
    final Cache<Long, Long> variationIdToItem = Caffeine.newBuilder().build();
    //map id -> bundles
    @NotNull
    final Cache<Long, List<PretixProductBundle>> itemIdToBundle = Caffeine.newBuilder().build();
    //map id -> quota
    @NotNull
    final Cache<Long, List<PretixQuota>> itemIdToQuota = Caffeine.newBuilder().build();
    //map id -> quota
    @NotNull
    final Cache<Long, List<PretixQuota>> variationIdToQuota = Caffeine.newBuilder().build();
    //map id -> room names
    @NotNull
    final Cache<Long, Map<String, String>> itemIdToNames = Caffeine.newBuilder().build();
    //map id -> room names
    @NotNull
    final Cache<Long, Map<String, String>> variationIdToNames = Caffeine.newBuilder().build();

    //Questions
    @NotNull
    final Cache<Long, QuestionType> questionIdToType = Caffeine.newBuilder().build();
    @NotNull
    final Cache<Long, String> questionIdToIdentifier = Caffeine.newBuilder().build();
    @NotNull
    final Cache<Long, List<PretixOption>> questionIdToOptions = Caffeine.newBuilder().build();

    //Tickets
    @NotNull
    final Cache<Long, Integer> dailyIdToDay = Caffeine.newBuilder().build(); //map id -> day idx

    //Sponsors
    @NotNull
    final Cache<Long, Sponsorship> sponsorshipIdToType = Caffeine.newBuilder().build();

    //Extra days
    @NotNull
    final Cache<Long, ExtraDays> extraDaysIdToDay = Caffeine.newBuilder().build();

    //Rooms
    //map id -> (capacity, hotelName, roomName)
    @NotNull
    final Cache<Long, HotelCapacityPair> roomIdToInfo = Caffeine.newBuilder().build();

    //Board
    // map id -> Board type
    @NotNull
    final Cache<Long, Board> variationIdToBoard = Caffeine.newBuilder().build();

    public void invalidate() {
        //General
        itemIdToPrice.invalidateAll();
        variationIdToPrice.invalidateAll();
        variationIdToItem.invalidateAll();
        itemIdToBundle.invalidateAll();
        itemIdToQuota.invalidateAll();
        variationIdToQuota.invalidateAll();
        itemIdToNames.invalidateAll();
        variationIdToNames.invalidateAll();

        //Questions
        questionIdToType.invalidateAll();
        questionIdToIdentifier.invalidateAll();
        questionIdToOptions.invalidateAll();

        //Tickets
        dailyIdToDay.invalidateAll();

        //Sponsors
        sponsorshipIdToType.invalidateAll();

        //Extra days
        extraDaysIdToDay.invalidateAll();

        //Rooms
        roomIdToInfo.invalidateAll();

        //Board
        variationIdToBoard.invalidateAll();
    }
}
