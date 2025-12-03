package net.furizon.backend.feature.pretix.objects.event.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.Event;
import net.furizon.backend.feature.pretix.objects.event.dto.SponsorshipNamesResponse;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class GetSponsorshipNamesUseCase implements UseCase<GetSponsorshipNamesUseCase.Input, SponsorshipNamesResponse> {

    @Override
    public @NotNull SponsorshipNamesResponse executor(@NotNull GetSponsorshipNamesUseCase.Input input) {
        //TODO add support for sponsor tiers from previous events
        PretixInformation pretix = input.pretixInformation;
        SponsorshipNamesResponse response = new SponsorshipNamesResponse();

        for (Sponsorship s : Sponsorship.values()) {
            if (s != Sponsorship.NONE) {
                var variationId = pretix.getSponsorIds(s).stream().findFirst();
                variationId.ifPresent(v -> response.sponsor(s, pretix.getVariationNames(v)));
            }
        }
        return response;
    }

    public record Input(
            long eventId,
            @NotNull PretixInformation pretixInformation
    ) {}
}
