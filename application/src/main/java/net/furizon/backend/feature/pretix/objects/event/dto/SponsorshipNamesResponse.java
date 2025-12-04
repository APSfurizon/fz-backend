package net.furizon.backend.feature.pretix.objects.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class SponsorshipNamesResponse {
    @NotNull
    private final List<Element> sponsors = new ArrayList<>(Sponsorship.values().length);

    @NotNull
    public SponsorshipNamesResponse sponsor(@NotNull Sponsorship sponsorship,
                                            @NotNull Map<String, String> sponsorNames) {
        sponsors.add(new Element(sponsorship, sponsorNames));
        return this;
    }

    @Data
    @AllArgsConstructor
    private static class Element {
        @NotNull private final Sponsorship sponsor;
        @NotNull private Map<String, String> sponsorNames;
    }
}
