package net.furizon.backend.feature.event;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.util.Pair;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Set;

@RequiredArgsConstructor
@Data
@Builder
public class Event {
    @NotNull
    private final String slug;

    @Nullable
    private final OffsetDateTime dateEnd;

    @Nullable
    private final OffsetDateTime dateFrom;

    @Nullable
    private final Boolean isCurrent;

    @Nullable
    private final String publicUrl;

    @Nullable
    private final Set<String> eventNames;

    @Nullable
    public Pair<String, String> getOrganizerAndEventPair() {
        if (publicUrl == null) {
            return null;
        }

        try {
            URI uri = new URI(publicUrl);

            // Split the path by "/" and filter out any empty parts
            String[] parts = uri.getPath().split("/");

            if (parts.length >= 3) {
                String organizer = parts[1];  // First part after base is the organizer
                String eventName = parts[2];  // Second part after base is the event name

                return Pair.of(organizer, eventName);
            } else {
                throw new IllegalArgumentException("URL does not contain organizer and event name");
            }
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
