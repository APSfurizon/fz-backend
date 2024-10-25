package net.furizon.backend.feature.pretix.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.furizon.backend.infrastructure.pretix.PretixGenericUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Map;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Event {
    @NotNull
    private final String slug;

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

}