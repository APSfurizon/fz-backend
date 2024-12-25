package net.furizon.backend.feature.pretix.objects.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.infrastructure.membership.MembershipYearUtils;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Slf4j
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Event {
    private final long id;

    @NotNull
    private String slug;

    @Nullable
    private OffsetDateTime dateTo;

    @Nullable
    private OffsetDateTime dateFrom;

    private boolean isCurrent;

    @NotNull
    private String publicUrl;

    @Nullable
    private Map<String, String> eventNames;

    public static class EventBuilder {
        public EventBuilder slug(String fullSlug) {
            this.slug = fullSlug;
            return this;
        }

        public EventBuilder slug(String eventSlug, String organizerSlug) {
            this.slug = PretixGenericUtils.buildOrgEventSlug(eventSlug, organizerSlug);
            return this;
        }
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
    public String toString() {
        return slug + "@" + id;
    }

    public short getMembershipYear(MembershipYearUtils membershipYearUtils) {
        OffsetDateTime from = dateFrom;
        if (from == null) {
            log.error("From date was unavailable for event {}. Falling back to Date.now()", slug);
            from = OffsetDateTime.now();
        }

        LocalDate date = from.toLocalDate();
        return membershipYearUtils.getMembershipYear(date);
    }
}
