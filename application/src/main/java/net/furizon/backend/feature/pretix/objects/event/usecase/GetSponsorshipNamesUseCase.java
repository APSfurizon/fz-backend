package net.furizon.backend.feature.pretix.objects.event.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.pretix.objects.event.dto.SponsorshipNamesResponse;
import net.furizon.backend.infrastructure.pretix.model.Sponsorship;
import net.furizon.backend.infrastructure.pretix.service.PretixInformation;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.BiConsumer;

@Component
@RequiredArgsConstructor
public class GetSponsorshipNamesUseCase implements UseCase<GetSponsorshipNamesUseCase.Input, SponsorshipNamesResponse> {

    @Override
    public @NotNull SponsorshipNamesResponse executor(@NotNull GetSponsorshipNamesUseCase.Input input) {
        PretixInformation pretix = input.pretixInformation;
        long eventId = input.eventId;
        SponsorshipNamesResponse response = new SponsorshipNamesResponse();

        loadSponsorNames(pretix, eventId, response::sponsor);
        return response;
    }

    public static void loadSponsorNames(@NotNull PretixInformation pretix, long eventId,
                                        @NotNull BiConsumer<Sponsorship, Map<String, String>> consumer) {
        for (Sponsorship s : Sponsorship.values()) {
            if (s != Sponsorship.NONE) {
                var variationId = pretix.getSponsorIds(s, eventId).stream().findFirst();
                variationId.ifPresent(v -> consumer.accept(s, pretix.getVariationNames(v)));
            }
        }
    }

    public record Input(
            long eventId,
            @NotNull PretixInformation pretixInformation
    ) {}
}
