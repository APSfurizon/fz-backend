package net.furizon.backend.feature.pretix.objects.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.localization.TranslationService;
import net.furizon.backend.infrastructure.membership.MembershipYearUtils;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import net.furizon.backend.infrastructure.pretix.model.ExtraDays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;


@Data
@Slf4j
@AllArgsConstructor
@Builder(toBuilder = true)
public class Event {
    private long id;

    @NotNull
    private String slug;

    @Nullable
    //dateFrom effectively set on pretix
    private OffsetDateTime pretixDateFrom;
    @Nullable
    //dateFrom eventually corrected so it correctly starts from the day after early arrival
    private OffsetDateTime correctDateFrom;

    @Nullable
    //dateTo effectively set on pretix
    private OffsetDateTime pretixDateTo;
    @Nullable
    //dateTo eventually corrected so it correctly starts from the day before late departure
    private OffsetDateTime correctDateTo;

    private boolean isCurrent;

    private boolean isLive;
    private boolean testModeEnabled;
    private boolean isPublic;

    @NotNull
    private String publicUrl;

    @Nullable
    private Map<String, String> eventNames;

    public @NotNull String getLocalizedName(@NotNull TranslationService translationService) {
        return translationService.getTranslationFromMap(eventNames);
    }

    public static class EventBuilder {
        public @NotNull EventBuilder slug(String fullSlug) {
            this.slug = fullSlug;
            return this;
        }

        public @NotNull EventBuilder slug(String eventSlug, String organizerSlug) {
            this.slug = PretixGenericUtils.buildOrgEventSlug(eventSlug, organizerSlug);
            return this;
        }

        public @NotNull EventBuilder dateFrom(@Nullable OffsetDateTime dateFrom, boolean earlyIncluded) {
            if (dateFrom != null) {
                this.correctDateFrom = computeCorrectDateFrom(dateFrom, earlyIncluded);
            }
            this.pretixDateFrom = dateFrom;
            return this;
        }

        public @NotNull EventBuilder dateTo(@Nullable OffsetDateTime dateTo, boolean checkoutIncluded) {
            if (dateTo != null) {
                this.correctDateTo = computeCorrectDateTo(dateTo, checkoutIncluded);
            }
            this.pretixDateTo = dateTo;
            return this;
        }
    }

    //TODO: EventIds are sequential. Check around we don't leak from the API params if a hidden event exists
    public boolean canBeShownToPublic() {
        return isPublic && isLive /*&& !testModeEnabled*/;
    }


    @Data
    public static class OrganizerAndEventPair {
        private final String organizer;
        private final String event;
    }

    @NotNull
    public OrganizerAndEventPair getOrganizerAndEventPair() {
        String[] sp = slug.split("/");
        return new OrganizerAndEventPair(sp[0], sp[1]);
    }

    @Override
    public @NotNull String toString() {
        return slug + "@" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Event e)) {
            return false;
        }
        return e.id == id;
    }

    public short getMembershipYear(MembershipYearUtils membershipYearUtils) {
        OffsetDateTime from = pretixDateFrom;
        if (from == null) {
            log.error("From date was unavailable for event {}. Falling back to Date.now()", slug);
            from = OffsetDateTime.now();
        }

        LocalDate date = from.toLocalDate();
        return membershipYearUtils.getMembershipYear(date);
    }

    public @NotNull Event setDateFrom(@Nullable OffsetDateTime dateFrom, boolean earlyIncluded) {
        if (dateFrom != null) {
            this.correctDateFrom = computeCorrectDateFrom(dateFrom, earlyIncluded);
        }
        this.pretixDateFrom = dateFrom;
        return this;
    }
    public @NotNull Event setDateTo(@Nullable OffsetDateTime dateTo, boolean checkoutIncluded) {
        if (dateTo != null) {
            this.correctDateTo = computeCorrectDateTo(dateTo, checkoutIncluded);
        }
        this.pretixDateTo = dateTo;
        return this;
    }

    // Correct to/from dates graphical explanation:
    //   When the event FROM should point to
    //    v
    //   EMMMMCL
    //        ^
    //       When the event TO should point to
    // (where E=Early arrival day, M=Main day, C=Checkout day, L=Late departure day)

    @NotNull
    public static OffsetDateTime computeCorrectDateFrom(@NotNull OffsetDateTime dateFrom, boolean earlyIncluded) {
        //Correct date means that event starts the day AFTER early arrival
        //If early is INcluded it means that the dateFrom set on pretix STARTS on the early day
        return earlyIncluded ? dateFrom.plusDays(ExtraDays.EARLY_DAYS_NO) : dateFrom;
    }
    @NotNull
    public static OffsetDateTime computeCorrectDateTo(@NotNull OffsetDateTime dateTo, boolean checkoutIncluded) {
        //Correct date means that event starts the day BEFORE late departure
        //If checkout is EXcluded it means that the dateTo set on pretix ENDS the day before checkout
        return checkoutIncluded ? dateTo : dateTo.plusDays(ExtraDays.CHECKOUT_DAYS_NO);
    }
}
