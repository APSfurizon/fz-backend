package net.furizon.backend.feature.event;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.util.Pair;

import java.time.OffsetDateTime;
import java.util.Set;

@RequiredArgsConstructor
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
    private Set<String> eventNames;

    public static class EventBuilder {
        public EventBuilder slug(String eventSlug, String organizerSlug) {
            this.slug = buildSlug(eventSlug, organizerSlug);
            return this;
        }
    }

    @NotNull
    public Pair<String, String> getOrganizerAndEventPair() {
        String[] sp = slug.split("/");
        return Pair.of(sp[0], sp[1]);
    }

    public static String buildSlug(String eventSlug, String organizerSlug) {
        return eventSlug + "/" + organizerSlug;
    }
}
