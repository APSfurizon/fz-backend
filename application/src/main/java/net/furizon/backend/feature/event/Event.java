package net.furizon.backend.feature.event;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
}
